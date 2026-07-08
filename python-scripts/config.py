"""Bootstrap config. Actual behavior config comes from Java over HTTP.

Only the Java base URL and terminal-display strings live here; anything that
controls behavior (API key, model, STT thresholds, language switching, debug
level) is fetched from /api/config and stored on the `runtime` object.
"""

import requests

# --- Bootstrap: the one thing Python has to know statically ---
JAVA_BASE_URL = "http://localhost:8080/api"
JAVA_URL = JAVA_BASE_URL + "/transcript"
AI_SETTINGS_URL = JAVA_BASE_URL + "/ai-settings"
CHAT_MESSAGE_URL = JAVA_BASE_URL + "/chat-message"
CONFIG_URL = JAVA_BASE_URL + "/config"


# --- Runtime config, filled in by refresh_config() ---
class Runtime:
    def __init__(self):
        # Fallbacks if Java is unreachable at startup.
        self.debug_level = 0
        self.api_key = ""
        self.ai_model = "gemini-2.5-flash"
        self.allow_ai_response = True

        self.language = "en-US"
        self.pause_threshold = 1.5
        self.phrase_threshold = 0.3
        self.non_speaking_duration = 0.5
        self.ambient_noise_adjustment = 3.0

        # code -> {"name", "aliases", "pleaseWait", "ready"}
        self.supported_languages = {}


runtime = Runtime()


def refresh_config():
    try:
        r = requests.get(CONFIG_URL, timeout=2)
        if r.status_code != 200:
            return False
        data = r.json() or {}
    except requests.exceptions.RequestException:
        return False

    runtime.debug_level = int(data.get("debugLevel", runtime.debug_level))
    runtime.api_key = data.get("apiKey", runtime.api_key) or runtime.api_key
    runtime.ai_model = data.get("aiModel", runtime.ai_model) or runtime.ai_model
    runtime.allow_ai_response = bool(data.get("allowAiResponse", runtime.allow_ai_response))

    runtime.language = data.get("sttLanguage", runtime.language) or runtime.language
    runtime.pause_threshold = float(data.get("pauseThreshold", runtime.pause_threshold))
    runtime.phrase_threshold = float(data.get("phraseThreshold", runtime.phrase_threshold))
    runtime.non_speaking_duration = float(data.get("nonSpeakingDuration", runtime.non_speaking_duration))
    runtime.ambient_noise_adjustment = float(data.get("ambientNoiseAdjustment", runtime.ambient_noise_adjustment))
    runtime.supported_languages = data.get("supportedLanguages", runtime.supported_languages) or {}
    return True


# --- Display strings kept in Python (UI text only) ---

SEND_TEXT = "   [Sending text to Java...]"
SEND_FULL_TRANSCRIPT = "    [Sending full session transcript to Java...]"
SEND_SUCCESS = "    [Successfully sent to Java!]"
SEND_FAILURE = "    [Error sending to Java...]"
SEND_FAILURE_JAVA_ERROR = " [Java server responded with error...]"
SEND_FAILURE_JAVA_CONNECT = "   [Failed to connect to Java server. Is it running?]"

AUDIO_PARSE_FAILURE = "   [... (Failed to understand audio) ...]"
GOOGLE_SERVICE_FAILURE = "   [Google STT Service error...]"

SPEECH_RECOGNITION_SETUP = "\n[!] Setting up speech recognition..."
SPEECH_RECOGNITION_READY = "[!] Ready to detect Speech!\n"

SESSION_TRANSCRIPT = "\n[+] Full session transcript: "

USER_SPEECH = "[User]: "
AI_THINKING = "AI is thinking..."
AI_ERROR = "AI Error..."
AI_CONNECTION_ERROR = "Sorry, I encountered an error connecting to the AI."
AI_RESPONSE = "[AI RESPONSE]: "
AI_SETTINGS_LOADED = "[AI] Loaded settings from Java backend."
AI_SETTINGS_FAILED = "[AI] Could not reach Java for settings — using defaults."
CONFIG_LOADED = "[config] Loaded runtime config from Java."
CONFIG_FAILED = "[config] Could not reach Java for config — using defaults."

DEFAULT_ROLE_INSTRUCTIONS = "You are a customer service agent. Assist customers with inquiries and provide accurate information."
DEFAULT_REPLY_INSTRUCTIONS = "Reply politely and concisely. If unsure, ask a clarifying question."
DEFAULT_ORGANIZATION_INFO = ""


# Pull config at import time so downstream modules (ai_agent, stt_sender) see real values.
if refresh_config():
    print(CONFIG_LOADED, flush=True)
else:
    print(CONFIG_FAILED, flush=True)
