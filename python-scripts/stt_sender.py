import time

import speech_recognition as sr
import requests

JAVA_URL = "http://localhost:8080/api/transcript"

full_transcript = []


def send(transcript, is_final=False):
    if not transcript:
        return

    label = "full session transcript" if is_final else "chunk"
    print(f"Sending {label} to Java...")
    try:
        payload = {"transcript": transcript, "final": is_final}
        response = requests.post(JAVA_URL, json=payload, timeout=5)

        if response.status_code == 200:
            print("Successfully sent to Java!")
        else:
            print(f"Java server responded with error: {response.status_code}")
    except requests.exceptions.ConnectionError:
        print("Failed to connect to Java server. Is it running?")
    except requests.exceptions.RequestException as e:
        print(f"Error sending to Java: {e}")


def handle_phrase(recognizer, audio):
    try:
        text = recognizer.recognize_google(audio)
        print(f"User said: {text}")
        full_transcript.append(text)
        send(text)
    except sr.UnknownValueError:
        print("... (Could not understand audio) ...")
    except sr.RequestError as e:
        print(f"Google STT Service error: {e}")


def listen():
    recognizer = sr.Recognizer()
    microphone = sr.Microphone()

    with microphone as source:
        print("\nSetting up speech recognition...")
        recognizer.adjust_for_ambient_noise(source, duration=1)
        print("\nReady to detect Speech!")

    recognizer.pause_threshold = 0.5

    stop_listening = recognizer.listen_in_background(microphone, handle_phrase)
    print("\nListening... Press Ctrl+C to stop.")

    try:
        while True:
            time.sleep(0.1)
    except KeyboardInterrupt:
        stop_listening(wait_for_stop=True)

        session_text = " ".join(full_transcript).strip()
        print(f"\nFull session transcript:\n{session_text}")
        send(session_text, is_final=True)


if __name__ == "__main__":
    listen()
