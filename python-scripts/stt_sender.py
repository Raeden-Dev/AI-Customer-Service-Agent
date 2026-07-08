import signal
import sys
import threading
import time
import traceback

import requests
import speech_recognition as sr

print("[stt_sender] starting up...", flush=True)

from ai_agent import get_ai_response, refresh_ai_settings
from config import (
    AI_RESPONSE,
    AUDIO_PARSE_FAILURE,
    CHAT_MESSAGE_URL,
    GOOGLE_SERVICE_FAILURE,
    JAVA_URL,
    SEND_FAILURE,
    SEND_FAILURE_JAVA_CONNECT,
    SEND_FAILURE_JAVA_ERROR,
    SEND_FULL_TRANSCRIPT,
    SEND_SUCCESS,
    SEND_TEXT,
    SESSION_TRANSCRIPT,
    SPEECH_RECOGNITION_READY,
    SPEECH_RECOGNITION_SETUP,
    USER_SPEECH,
    refresh_config,
    runtime,
)

full_transcript = []
_stop_listening = None
_current_language = runtime.language
_switch_in_progress = False
_switch_lock = threading.Lock()


def send(transcript, is_final=False):
    if not transcript:
        return

    if is_final:
        print(SEND_FULL_TRANSCRIPT, flush=True)
    elif runtime.debug_level >= 2:
        print(SEND_TEXT, flush=True)

    try:
        payload = {"transcript": transcript, "final": is_final}
        response = requests.post(JAVA_URL, json=payload, timeout=1)

        if response.status_code == 200 and runtime.debug_level >= 2:
            print(SEND_SUCCESS, flush=True)
        elif runtime.debug_level >= 1:
            print(SEND_FAILURE_JAVA_ERROR, flush=True)
            print(response.status_code, flush=True)
    except requests.exceptions.ConnectionError:
        if runtime.debug_level >= 1:
            print(SEND_FAILURE_JAVA_CONNECT, flush=True)
    except requests.exceptions.RequestException as e:
        if runtime.debug_level >= 1:
            print(SEND_FAILURE, flush=True)
            print(e, flush=True)


def send_chat_message(role, content):
    """Push a chat message (user/ai/system) to Java so it shows up in the call log."""
    if not content:
        return
    try:
        requests.post(CHAT_MESSAGE_URL, json={"role": role, "content": content}, timeout=1)
    except requests.exceptions.RequestException as e:
        if runtime.debug_level >= 1:
            print(f"{SEND_FAILURE} (chat-message: {e})", flush=True)


def _detect_language_switch(text):
    """Return a target language code if the text is a switch command, else None."""
    lower = text.lower().strip()
    for code, meta in runtime.supported_languages.items():
        if code == _current_language:
            continue
        for alias in meta.get("aliases", []):
            if alias and alias.lower() in lower:
                return code
    return None


def _start_listener():
    global _stop_listening
    recognizer = sr.Recognizer()
    microphone = sr.Microphone()

    with microphone as source:
        print(SPEECH_RECOGNITION_SETUP, flush=True)
        recognizer.adjust_for_ambient_noise(source, duration=runtime.ambient_noise_adjustment)
        print(SPEECH_RECOGNITION_READY, flush=True)

    recognizer.pause_threshold = runtime.pause_threshold
    recognizer.phrase_threshold = runtime.phrase_threshold
    recognizer.non_speaking_duration = runtime.non_speaking_duration

    _stop_listening = recognizer.listen_in_background(microphone, handle_phrase)


def _do_switch(new_lang):
    global _current_language, _stop_listening, _switch_in_progress
    try:
        meta = runtime.supported_languages.get(new_lang, {})
        please_wait = meta.get("pleaseWait") or f"Please wait, switching to {new_lang}..."
        ready = meta.get("ready") or f"Ready. You can now speak in {new_lang}."

        send_chat_message("ai", please_wait)
        send_chat_message("system", f"Switching STT language from {_current_language} to {new_lang}...")

        old_stop = _stop_listening
        _stop_listening = None
        if old_stop is not None:
            try:
                old_stop(wait_for_stop=True)
            except Exception:
                pass

        _current_language = new_lang
        _start_listener()

        send_chat_message("ai", ready)
        print(f"[stt_sender] language switched to {new_lang}", flush=True)
    except Exception:
        print("[stt_sender] language switch failed:", flush=True)
        traceback.print_exc()
        sys.stdout.flush()
    finally:
        with _switch_lock:
            _switch_in_progress = False


def switch_language(new_lang):
    global _switch_in_progress
    with _switch_lock:
        if _switch_in_progress or new_lang == _current_language:
            return False
        _switch_in_progress = True
    threading.Thread(target=_do_switch, args=(new_lang,), daemon=True).start()
    return True


def handle_phrase(recognizer, audio):
    if _switch_in_progress:
        return
    try:
        text = recognizer.recognize_google(audio, language=_current_language)
        print(USER_SPEECH + text, flush=True)
        full_transcript.append(text)
        send_chat_message("user", text)

        target_lang = _detect_language_switch(text)
        if target_lang is not None:
            switch_language(target_lang)
            return

        if runtime.allow_ai_response:
            answer = get_ai_response(text)
            print(AI_RESPONSE + answer, flush=True)
            send_chat_message("ai", answer)
    except sr.UnknownValueError:
        print(AUDIO_PARSE_FAILURE, flush=True)
    except sr.RequestError as e:
        print(GOOGLE_SERVICE_FAILURE, flush=True)
        print(e, flush=True)


def _finalize():
    global _stop_listening
    if _stop_listening is not None:
        try:
            _stop_listening(wait_for_stop=False)
        except Exception:
            pass
        _stop_listening = None
    session_text = " ".join(full_transcript).strip()
    print(SESSION_TRANSCRIPT + session_text, flush=True)
    send(session_text, is_final=True)


def _on_signal(signum, _frame):
    print(f"\n[stt_sender] Received signal {signum}, finalizing call...", flush=True)
    _finalize()
    sys.exit(0)


def listen():
    global _current_language
    # Pull fresh config + AI settings at the start of each call.
    refresh_config()
    refresh_ai_settings()
    _current_language = runtime.language
    print(f"[stt_sender] starting with language={_current_language}", flush=True)
    _start_listener()
    try:
        while True:
            time.sleep(0.1)
    except KeyboardInterrupt:
        _finalize()


if __name__ == "__main__":
    signal.signal(signal.SIGINT, _on_signal)
    if hasattr(signal, "SIGTERM"):
        signal.signal(signal.SIGTERM, _on_signal)
    if hasattr(signal, "SIGBREAK"):
        signal.signal(signal.SIGBREAK, _on_signal)
    try:
        listen()
    except Exception:
        print("[stt_sender] fatal error:", flush=True)
        traceback.print_exc()
        sys.stdout.flush()
        sys.exit(1)
