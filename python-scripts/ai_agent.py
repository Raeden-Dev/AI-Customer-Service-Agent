"""The AI brain of a call.

For every phrase the customer says, this module:
  1. builds one big text prompt out of:
       - the AI persona            (from Java: /api/ai-settings)
       - the current call mode     (from Java: /api/call-context)
       - the business knowledge    (intelligence.json, via call-context)
       - the client profile        (clients.json, via call-context)
       - the conversation so far   (kept here, in memory)
  2. sends it to Google Gemini and takes the reply,
  3. checks the reply for a mode tag like [MODE:WRONG_NUMBER] — if the AI
     flagged one, the tag is stripped from the spoken text and the switch
     is reported back to Java over POST /api/call-mode.
"""

import re

import requests
from google import genai

from config import (
    AI_CONNECTION_ERROR,
    AI_ERROR,
    AI_SETTINGS_FAILED,
    AI_SETTINGS_LOADED,
    AI_SETTINGS_URL,
    AI_THINKING,
    CALL_CONTEXT_FAILED,
    CALL_CONTEXT_LOADED,
    CALL_CONTEXT_URL,
    CALL_MODE_URL,
    DEFAULT_ORGANIZATION_INFO,
    DEFAULT_REPLY_INSTRUCTIONS,
    DEFAULT_ROLE_INSTRUCTIONS,
    MODE_REPORTED,
    runtime,
)

_client = None

# The AI writes e.g. "...goodbye. [MODE:WRONG_NUMBER]" to flag a scenario switch.
MODE_TAG_PATTERN = re.compile(r"\[MODE:([A-Z_]+)\]")

# How many past lines of dialog are replayed into each prompt.
HISTORY_LIMIT = 20


def client_for_key(api_key):
    """Cache one genai client per process."""
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

# Call context pulled from Java (mode + business/client knowledge + greeting).
call_context = {
    "mode": "NEW_CUSTOMER",
    "modeName": "New Customer",
    "modeInstructions": "",
    "modeSwitchInstructions": "",
    "businessName": "",
    "businessInfo": "",
    "clientInfo": "",
    "greeting": "",
}

# The dialog so far, e.g. ["Customer: hi", "Agent: hello!"]. Newest last.
conversation_history = []


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


def refresh_call_context():
    """Fetch the current call's mode + business/client knowledge from Java."""
    try:
        response = requests.get(CALL_CONTEXT_URL, timeout=2)
        if response.status_code == 200:
            data = response.json() or {}
            for key in call_context:
                if key in data and data[key] is not None:
                    call_context[key] = data[key]
            if runtime.debug_level >= 1:
                print(CALL_CONTEXT_LOADED.format(mode=call_context["mode"]), flush=True)
            return True
    except requests.exceptions.RequestException:
        pass

    if runtime.debug_level >= 1:
        print(CALL_CONTEXT_FAILED, flush=True)
    return False


def remember(speaker, text):
    """Store one line of dialog so later prompts include it."""
    if not text:
        return
    conversation_history.append(f"{speaker}: {text}")
    # Keep only the newest lines so the prompt cannot grow forever.
    del conversation_history[:-HISTORY_LIMIT]


def reset_conversation():
    """Forget the dialog (called when a new call starts)."""
    conversation_history.clear()


def build_prompt(user_text):
    """Glue every piece of context into one prompt for Gemini."""
    parts = [
        "[Role]",
        ai_settings["roleInstructions"],
        "",
        "[Reply Guidelines]",
        ai_settings["replyInstructions"],
        "Your reply will be spoken out loud to the caller, so answer in plain",
        "sentences without lists, markdown or emojis, and keep it short.",
    ]

    if call_context["modeInstructions"]:
        parts += ["", f"[Current Call Mode: {call_context['modeName']}]",
                  call_context["modeInstructions"]]
    if call_context["modeSwitchInstructions"]:
        parts += ["", "[Mode Switching]", call_context["modeSwitchInstructions"]]
    if call_context["businessInfo"]:
        parts += ["", "[Business Info]", call_context["businessInfo"]]
    if ai_settings["organizationInfo"]:
        parts += ["", "[Organization Info]", ai_settings["organizationInfo"]]
    if call_context["clientInfo"]:
        parts += ["", "[Client Info — the person you are talking to]",
                  call_context["clientInfo"]]
    if conversation_history:
        parts += ["", "[Conversation so far]"] + conversation_history

    parts += ["", "[Customer said]", user_text, "", "Reply as the agent:"]
    return "\n".join(parts)


def extract_mode_tag(reply_text):
    """Split a reply into (clean_text, mode). mode is None when no tag was used."""
    if not reply_text:
        return "", None
    match = MODE_TAG_PATTERN.search(reply_text)
    mode = match.group(1) if match else None
    clean = MODE_TAG_PATTERN.sub("", reply_text).strip()
    return clean, mode


def report_mode_change(mode):
    """Tell Java the AI switched scenario, then re-pull the mode instructions."""
    try:
        requests.post(CALL_MODE_URL, json={"mode": mode, "reason": "AI detected"}, timeout=2)
        if runtime.debug_level >= 1:
            print(MODE_REPORTED.format(mode=mode), flush=True)
    except requests.exceptions.RequestException:
        pass
    refresh_call_context()


def get_ai_response(text_prompt):
    """Ask Gemini for a reply. Returns (reply_text, new_mode_or_None)."""
    if runtime.debug_level >= 2:
        print(AI_THINKING, flush=True)

    remember("Customer", text_prompt)
    try:
        client = client_for_key(runtime.api_key)
        response = client.models.generate_content(
            model=ai_settings["model"] or runtime.ai_model,
            contents=build_prompt(text_prompt),
        )
        raw_reply = response.text or ""
    except Exception as e:
        if runtime.debug_level >= 1:
            print(AI_ERROR, flush=True)
            print(e, flush=True)
        return AI_CONNECTION_ERROR, None

    reply, new_mode = extract_mode_tag(raw_reply)
    if new_mode:
        report_mode_change(new_mode)
    remember("Agent", reply)
    return reply, new_mode


refresh_ai_settings()
refresh_call_context()
