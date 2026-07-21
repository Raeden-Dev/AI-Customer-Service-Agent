# AI Customer Service Agent

A voice-based customer service agent. You speak into the microphone, the app
turns your speech into text, an AI (Google Gemini) writes a reply using the
business's own knowledge files, and the reply is **spoken back out loud**.

Everything is stored as simple JSON files — no database needed.

## How it works (short version)

```
Your voice → Google Speech-to-Text → Gemini AI → reply text → Text-to-Speech (spoken back)
                                        ↑
                     business + client knowledge from JSON files
```

Two programs cooperate:

| Part | Folder | Job |
|------|--------|-----|
| **Java backend** (Spring Boot) | `java-backend/` | The operator console, all data files, the REST API on `localhost:8080` |
| **Python scripts** | `python-scripts/` | Microphone, speech-to-text, the Gemini call, text-to-speech |

Java starts the Python process when a call starts and stops it when the call
ends. They only talk over HTTP (`/api/...`).

## Setup

You need: **Java 21+**, **Maven**, **Python 3.10+**, a microphone and speakers.

```bash
# 1. Python dependencies (one time)
cd python-scripts
python -m venv .venv
.venv\Scripts\pip install -r requirements.txt        # Windows

# 2. Put your Gemini API key in java-backend/data/config.json  ("apiKey": "...")

# 3. Run the backend (it launches Python by itself during calls)
cd ../java-backend
mvn spring-boot:run
```

## Console commands

The app gives you an `agent>` prompt:

| Command | What it does |
|---------|--------------|
| `help` | Show all commands |
| `status` | Show the active call (mode, caller, transcript) |
| `businesses` | List registered businesses |
| `add-business <name>` | Register a new business |
| `use <name>` | Pick the active business |
| `start-call` | Start a call as an **unknown caller** (New Customer mode) |
| `start-call <client_id>` | Start a call as a **known client** (Existing Customer mode), e.g. `start-call C001` |
| `end-call` | End the call and save history + transcript |
| `clients` | List the clients of the active business |
| `intel` | Print the business knowledge the AI receives |
| `set-mode <mode>` | Force a call mode by hand (for testing) |
| `ai` | Show the AI persona settings |
| `config` | Show global config |
| `refresh` | Re-read all JSON files from disk |
| `exit` | Shut down |

## The four call modes

| Mode | How it starts | What the AI does |
|------|--------------|------------------|
| **New Customer** | `start-call` | Greets, asks the caller's name and needs, explains services |
| **Existing Customer** | `start-call <client_id>` | Greets the client by name, uses their notes and past issues |
| **Wrong / Scam Number** | AI detects it mid-call (or `set-mode wrong_number`) | Stays polite, shares nothing, ends the call |
| **Complex Request** | AI detects it mid-call (or `set-mode complex_request`) | Says a human agent will take over; the AI then stays quiet (simulated hand-over) |

Mid-call detection works with tags: the AI is told it may end a reply with
`[MODE:WRONG_NUMBER]` or `[MODE:COMPLEX_REQUEST]`. Python strips the tag
before speaking and reports the switch to Java (`POST /api/call-mode`).

## Data files (the "database")

```
java-backend/data/
├── config.json                     global settings (API key, STT + TTS tuning)
└── businesses/<name>/
    ├── business.json               id, name, contact details
    ├── ai-settings.json            AI persona (model, role, reply style)
    ├── intelligence.json           what the AI knows about the business ★
    ├── clients.json                the business's clients ★
    ├── call-history.json           every finished call
    └── transcripts/<callId>.txt    readable transcript per call
```

★ = the knowledge files you edit to make the AI smarter. They are re-read at
the start of every call, so edit → `start-call` is enough (no restart).

**`intelligence.json`** — business knowledge: `about` (one paragraph),
`services` (list), `policies` (list), `faqs` (question/answer pairs).

**`clients.json`** — per-client intelligence: each client has a `clientId`
(what you type in `start-call C001`), `name`, contact info, `notes` free text,
and `pastIssues` (list of earlier problems). New businesses get starter
template files automatically.

## Text-to-speech settings

In `data/config.json` (applied at the next call):

| Field | Meaning |
|-------|---------|
| `ttsEnabled` | `false` = replies are text-only |
| `ttsRate` | speaking speed, words per minute (170 ≈ natural) |
| `ttsVolume` | `0.0` – `1.0` |
| `ttsVoice` | part of an installed voice name (e.g. `"Zira"`), empty = default |

TTS runs offline through the operating system's voices (pyttsx3).
Run `python tts_speaker.py` inside `python-scripts` to list your voices
and hear a test sentence.

## REST API (Java ⇄ Python)

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/config` | Python fetches global settings at call start |
| POST | `/api/config` | Overwrite global settings |
| GET | `/api/ai-settings` | AI persona of the active business |
| POST | `/api/ai-settings` | Update the persona |
| GET | `/api/call-context` | Mode instructions + business info + client info + greeting |
| POST | `/api/call-mode` | Python reports a mode switch flagged by the AI |
| POST | `/api/chat-message` | Every live line (user / ai / system) |
| POST | `/api/transcript` | Full transcript, sent once at call end |

## Project layout

```
java-backend/src/main/java/com/ulab/agent/
├── Main.java          entry point
├── ai/                CallMode enum (the 4 scenarios + AI instructions)
├── api/               all REST controllers + request/response classes
├── managers/          the logic: Config, Business, AISettings, Call, Intelligence
├── models/            data classes stored as JSON
└── utils/             console, file/path/time helpers, all UI strings (Lang)

python-scripts/
├── stt_sender.py      microphone loop + language switching (main file)
├── ai_agent.py        builds the prompt, calls Gemini, detects mode tags
├── tts_speaker.py     speaks text out loud
└── config.py          fetches runtime config from Java
```

More detail: open `report_dashboard.html` in a browser.

## Notes

- The AI remembers the conversation during one call (last 20 lines) and
  forgets it when the call ends.
- While the AI is speaking, the microphone is not listening — this stops the
  AI from hearing itself.
- Keep your real API key out of public repos: `data/config.json` is meant for
  local use.
