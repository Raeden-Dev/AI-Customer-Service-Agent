import requests
from google import genai

from config import (
    AI_CONNECTION_ERROR,
    AI_ERROR,
    AI_RESPONSE,
    AI_SETTINGS_FAILED,
    AI_SETTINGS_LOADED,
    AI_SETTINGS_URL,
    AI_THINKING,
    DEFAULT_ORGANIZATION_INFO,
    DEFAULT_REPLY_INSTRUCTIONS,
    DEFAULT_ROLE_INSTRUCTIONS,
    runtime,
)

_client = None


def client_for_key(api_key):
    """Cache one genai client per api key."""
    global _client
    if _client is None:
        _client = genai.Client(api_key=api_key)
    return _client


# Settings pulled from Java (per active business).
ai_settings = {
    "model": runtime.ai_model,
    "roleInstructions": DEFAULT_ROLE_INSTRUCTIONS,
    "replyInstructions": DEFAULT_REPLY_INSTRUCTIONS,
    "organizationInfo": DEFAULT_ORGANIZATION_INFO,
}


def refresh_ai_settings():
    """Fetch AI settings for the currently-active business from Java."""
    try:
        response = requests.get(AI_SETTINGS_URL, timeout=2)
        if response.status_code == 200:
            data = response.json() or {}
            for key in ai_settings:
                value = data.get(key)
                if value:
                    ai_settings[key] = value
            if runtime.debug_level >= 1:
                print(AI_SETTINGS_LOADED, flush=True)
            return True
    except requests.exceptions.RequestException:
        pass

    if runtime.debug_level >= 1:
        print(AI_SETTINGS_FAILED, flush=True)
    return False


def build_prompt(user_text):
    parts = [
        "[Role]",
        ai_settings["roleInstructions"],
        "",
        "[Reply Guidelines]",
        ai_settings["replyInstructions"],
    ]
    if ai_settings["organizationInfo"]:
        parts += ["", "[Organization Info]", ai_settings["organizationInfo"]]
    parts += ["", "[Customer said]", user_text]
    return "\n".join(parts)


def get_ai_response(text_prompt):
    if runtime.debug_level >= 2:
        print(AI_THINKING, flush=True)
    try:
        client = client_for_key(runtime.api_key)
        response = client.models.generate_content(
            model=ai_settings["model"] or runtime.ai_model,
            contents=build_prompt(text_prompt),
        )
        return response.text
    except Exception as e:
        if runtime.debug_level >= 1:
            print(AI_ERROR, flush=True)
            print(e, flush=True)
        return AI_CONNECTION_ERROR


refresh_ai_settings()
