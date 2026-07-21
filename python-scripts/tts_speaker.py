"""Text-to-speech: gives the AI a voice.

Uses pyttsx3, which talks to the voices built into the operating system
(SAPI5 on Windows), so it works offline and needs no API key.

Settings come from Java's config (data/config.json):
    ttsEnabled  - master on/off switch
    ttsRate     - speaking speed in words per minute (~170 sounds natural)
    ttsVolume   - 0.0 (silent) to 1.0 (full)
    ttsVoice    - part of a voice name, e.g. "Zira"; empty picks the default

Design note: speak() BLOCKS until the sentence is finished. That is on
purpose — the microphone loop and the speaker share one thread, so the mic
is not listening while the AI talks. That stops the AI from hearing and
answering itself through the speakers.

We also create a fresh engine for every sentence. Re-using one engine
across threads is a known source of "it only speaks once" bugs in pyttsx3;
creating it per call is a little slower but always works.
"""

import pyttsx3

from config import TTS_ERROR, runtime


def speak(text):
    """Say the text out loud. Does nothing if TTS is disabled or text is empty."""
    if not runtime.tts_enabled or not text:
        return
    try:
        engine = pyttsx3.init()
        engine.setProperty("rate", int(runtime.tts_rate))
        engine.setProperty("volume", float(runtime.tts_volume))
        _apply_voice(engine)
        engine.say(text)
        engine.runAndWait()   # blocks until speaking is done (see note above)
        engine.stop()
    except Exception as e:
        if runtime.debug_level >= 1:
            print(TTS_ERROR, e, flush=True)


def _apply_voice(engine):
    """Pick the configured voice, matched by name substring (case-insensitive)."""
    wanted = (runtime.tts_voice or "").strip().lower()
    if not wanted:
        return
    for voice in engine.getProperty("voices"):
        if wanted in (voice.name or "").lower():
            engine.setProperty("voice", voice.id)
            return


def list_voices():
    """Prints the voices installed on this machine (handy for picking ttsVoice)."""
    engine = pyttsx3.init()
    for voice in engine.getProperty("voices"):
        print(voice.name, flush=True)
    engine.stop()


if __name__ == "__main__":
    # Quick manual test:  python tts_speaker.py
    list_voices()
    speak("Hello! This is the customer service agent voice test.")
