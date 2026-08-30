# ══════════════════════════════════════════════════════════════════════════════
# MASTER BUILD PROMPT — "BRAINING" (فهم)
# Arabic Voice-Commanded AI Orchestrator + Prompt-Forge + Optional PC Agent Bridge
# Deliverable: a standalone Android APK, installable on ANY Android device.
# ══════════════════════════════════════════════════════════════════════════════

> ## ⚠ STATUS BANNER — added 2026-08-17, read before §0
>
> **This document is the original master prompt. It is history, not status.** Everything in §0
> below was carried out in July 2026: the Understanding Brief, the Questions, the Suggestions and
> the Permissions list were produced and **answered**, and those answers are `ANSWERS.md` —
> which **overrides this file wherever the two disagree**.
>
> **Do not restart §0. Do not re-run `scripts/install-skills.sh`. Do not treat §12's milestones
> as work not yet begun.**
>
> Where this file is already superseded:
>
> - **§5 providers** — GitHub Models is gone; the fourth provider is Google Gemini
>   (`ANSWERS.md` Part 1 §3 and Part 8 §D1).
> - **§5 "Claude is the default brain" for CLARIFY** — narrowed to a *recommendation*; the brain
>   is whichever provider is selected on screen (`ANSWERS.md` Part 7 §M3-3).
> - **§2 "for every request"** — CLARIFY is a mode you enter, not a gate on every message
>   (`ANSWERS.md` Part 7 §M3-1).
> - **§9 free-by-default STT** — voice runs on Deepgram, BYOK, because on-device Arabic lost a
>   third of the words (`ANSWERS.md` Part 6 §M2-10).
> - **§12 milestones** — M1, M2 and M3 are **closed**. M4 is next.
>
> **The live state of the build is `PROJECT_STATE.md`.** Start there.

## 0. HOW TO USE THIS PROMPT — READ FIRST, DO NOT CODE YET

You are the lead engineer for this project. Before writing ANY code, you MUST:

1. Read this ENTIRE document AND the companion files in this repo:
   `README.md`, `docs/ARCHITECTURE.md`, `docs/PROMPT_FRAMEWORKS.md`,
   `docs/SKILLS.md`, `docs/SETUP.md`, and `.opencode/instructions.md`.
2. Install the required AGENT SKILLS into your OpenCode environment by running
   `scripts/install-skills.sh` (see §10 and `docs/SKILLS.md`). Do this BEFORE
   designing, so the skills inform your work. List any skill slug you had to
   substitute.
3. Produce an **UNDERSTANDING BRIEF** in your own words: the mission, the two
   execution paths, the request lifecycle, the standalone-APK model, and the
   milestone plan.
4. Produce a **QUESTIONS LIST**: every ambiguity, gap, or decision you want the
   owner to settle before you start. Do not be shy — ask about everything unclear.
5. Produce a **SUGGESTIONS LIST**: improvements, risks, simpler alternatives, and
   anything you'd do differently as an expert. Do not hold back a single idea.
6. Produce a **PERMISSIONS & PREP LIST**: exactly what OS permissions, accounts,
   tools, and config changes are needed on BOTH the Android phone and the Windows
   PC — designed so setup takes the user as little time and manual work as possible.
7. STOP and wait for the owner's answers and approval. Only then begin Milestone 1.

This "analyze-first, execute-once" discipline is the #1 rule of this project.
The owner's philosophy: *"An hour of analysis saves days of execution."* Honor it
literally — over-clarify before acting, never under-clarify. This same discipline
must be built INTO the app itself (the CLARIFY stage, §2.3).

## 1. MISSION

Build a personal, single-user system — centered on an Android app shipped as a
standalone APK — that turns spoken Arabic requests into fully-formed ideas (via
interactive dialogue), then into professional English prompts (using established
prompt-engineering frameworks), then executes them, then returns results translated
back into Arabic, and captures the owner's spoken feedback — all orchestrated across
several AI providers, each used where it is strongest.

The name of the project is **Braining** (Arabic: **فهم**). Use this name in the app
title, package identity, and all user-facing text.

## 2. THE REQUEST LIFECYCLE (the spine of the whole system)

For every request, the system moves through these stages:

1. CAPTURE  — User speaks a request in the Android app (hold-to-talk).
2. TRANSCRIBE — Audio → Arabic text. Show it; let the user edit it.
3. CLARIFY (core stage) — The system ANALYZES the request and opens an interactive
   dialogue: asks questions, discusses the idea fully, offers suggestions, raises
   notes and caveats. It does NOT proceed until the user explicitly declares the
   idea "ready/mature" (نضجت الفكرة).
4. FORGE — The system selects the most suitable professional prompt framework(s)
   for this task type and generates a rigorous ENGLISH prompt. See
   `docs/PROMPT_FRAMEWORKS.md`.
5. ROUTE & EXECUTE — The prompt runs via the correct execution path (see §3).
6. TRANSLATE — Outputs are translated into Arabic and shown to the user.
7. FEEDBACK — User sends spoken feedback → loops back to refine the prompt or the
   execution, with FULL session context preserved.
8. PERSIST — Everything is saved to searchable history.

## 3. TWO EXECUTION PATHS (this resolves the "PC is off" reality)

| | PATH A — Direct API | PATH B — PC Agent Bridge |
|---|---|---|
| Works while PC is OFF? | ✅ YES | ❌ NO (needs PC powered on) |
| Use cases | Deep/broad web research, summarizing, analysis, consultation, discussion, generating files as text (docs, sheets, code-as-text) | Building large software projects on the user's real files, delegating complex tasks that act on the PC's disk |
| Mechanism | Runs entirely inside the Android app via the user's API keys | A lightweight local server on Windows drives OpenCode in non-interactive mode |

RULES:
- The router MUST first classify each request as Path A or Path B.
- Path A is the self-sufficient core and must work with the PC completely off.
- If a Path B request arrives while the PC is unreachable, the app says clearly
  "PC not connected — this task needs it" and offers to queue it for later.
- Build and fully validate ALL of Path A before building Path B.

## 4. STANDALONE APK & MULTI-USER PORTABILITY (new, critical)

The deliverable is a **standalone signed APK** that the owner can share and that
runs on ANY Android device, fully independent of the owner's specific PC.

- NO hardcoded endpoints, keys, IPs, or PC identity. Everything user-specific is
  entered at runtime in the in-app Setup screen and stored encrypted on-device.
- Each new user connects the app to THEIR OWN PC (for Path B) via a simple pairing
  flow — never to the owner's machine.
- PAIRING FLOW must be dead-simple: the user runs one bootstrap script on their
  Windows PC (`scripts/setup.ps1`); it starts the local bridge inside Tailscale and
  prints a QR code / short pairing code; the user scans/enters it in the app once.
  Done. See `docs/SETUP.md`.
- Path A works immediately on any device with just API keys — no PC needed at all.
- Build a release-signed APK and document the exact `./gradlew assembleRelease`
  (or bundle) command and where the APK lands. Provide plain install steps for
  sideloading (enable "install unknown apps", open APK, install).
- Provide `docs/SETUP.md` in BOTH Arabic and English covering: install the APK,
  enter API keys, (optional) pair with a PC in a few steps.

## 5. MODEL ORCHESTRATION (coordinating multiple tools)

- Connected providers (via each user's own API keys): Anthropic (Claude), OpenAI
  (ChatGPT), DeepSeek, and **GitHub Models** in place of "Copilot".
  IMPORTANT: GitHub Copilot has NO general text-generation public API — it is an
  editor code-completion product. Do NOT build a Copilot provider. Use GitHub Models
  if available; otherwise leave a clearly-labeled, non-blocking stub in Settings.
- DEFAULT BRAIN: Use Claude as the default model for the CLARIFY, FORGE, and TRANSLATE
  layers (strongest at prompt-engineering and Arabic). This is a default, not a lock.
- The router MAY pick a cheaper/faster model for the classification step itself.
- Every routing decision is TRANSPARENT: show which model handled the request + a
  one-line rationale. Let the user override the model on any connected provider and
  re-run. Provider fallback on timeout/rate-limit/error is mandatory.
- The AI-router itself must be a TOGGLE (user can disable it and use the plain
  rule-based table).

## 6. PROMPT-FORGE & CONTEXT ENGINEERING LAYER

- Maintain a **library of templated prompt frameworks**, indexed by task type
  (build / research / automation / agent / consultation / discussion / business /
  finance / digital-transformation / scientific-research / planning / teaching).
  Full catalogue and selection heuristics live in `docs/PROMPT_FRAMEWORKS.md`.
- Use recognized frameworks as building blocks — e.g. CO-STAR and RTF for structure,
  ReAct for agentic/tool-using tasks, Chain-of-Thought / decomposition for reasoning,
  and Anthropic-style structure (XML tags + few-shot examples). NOTE: "most suitable
  framework" is a heuristic choice, not a rigid rule; make the chosen framework
  visible and editable by the user.
- Preserve FULL SESSION CONTEXT for every request: original transcript + refined idea
  + chosen framework + generated prompt + execution log + user feedback. Loop
  refinement must operate with complete memory of the session. This is the heart of
  the "context engineering" the owner asked for.

## 7. PC AGENT BRIDGE (Path B) — architecture

- Channel: a lightweight local server on the Windows PC (REST + WebSocket) that
  receives structured commands from the Android app and drives an agent in
  non-interactive/headless mode.
- Remote reachability from anywhere: wrap the local server inside a **Tailscale**
  private network (free for personal use, opens NO ports to the public internet).
  NOTE: Phone Link and TeamViewer are the WRONG tools here — Phone Link has no
  programmable API for this, and TeamViewer is screen-control, not a command
  channel. Tailscale is the correct replacement.
- Agent of choice: **OpenCode itself is the FIRST choice** — configure its settings
  as you (the agent) see fit for this project and the user's future projects. Claude
  Code is the fallback.
  (Verify your installed OpenCode version supports a headless/server mode before
  relying on it; if not, raise it in the QUESTIONS LIST.)
- THREE MANDATORY GUARDRAILS on the PC:
  1. FULL REPORT of every change — any file created/modified/deleted, any system or
     config setting changed, and a complete log of what was done. Deliver this report
     back to the user in the app after each PC task.
  2. APPROVAL GATE before any destructive action (delete / bulk-edit / system command).
  3. WORKING-DIRECTORY CONFINEMENT — the agent operates only inside a user-approved
     project folder unless explicitly widened.

## 8. TECH STACK

- Android: Kotlin, Jetpack Compose, Material 3, FULL RTL/Arabic support.
- Architecture: Clean Architecture (data / domain / presentation) + MVVM, multi-module.
- Async/streaming: Coroutines + Flow; token-by-token SSE streaming; all I/O off main thread.
- DI: Hilt. Networking: Ktor client with SSE. Persistence: Room (history) + DataStore
  (settings). Keys: Android Keystore-backed encryption.
- Min SDK 26, target latest stable SDK. Produce a release-signed APK.
- PC bridge: lightweight server (minimal Kotlin/Ktor or Node — pick the simplest to
  install), Tailscale, OpenCode headless.

## 9. FREE-BY-DEFAULT SPEECH-TO-TEXT

- Default: ON-DEVICE transcription (free, works with the PC off).
- Optional upgrade when the PC is reachable: route audio to `whisper.cpp` running on
  the PC (most accurate for Arabic, zero API cost). Fully free end-to-end.
- No paid transcription API is required. If the on-device engine choice matters,
  raise it in the QUESTIONS LIST.

## 10. AGENT SKILLS — install BEFORE building (see docs/SKILLS.md)

Install the full skill set that raises quality across ALL of the owner's domains:
AI software engineering, systems analysis, English-language teaching, idea/project
discussion, entrepreneurship & finance, automation systems, digital-transformation
projects, research & scientific work, planning, and complementary skills.

Run:
```bash
bash scripts/install-skills.sh
```
These are AGENT SKILLS (procedural knowledge for you, the building agent) — NOT
Android dependencies. Android runtime dependencies (Ktor, Hilt, Room, etc.) are
declared in Gradle as usual. If any skill slug has changed in the directory, install
the closest current equivalent and note the substitution.

## 11. FRICTIONLESS SETUP REQUIREMENT (minimize user time & effort)

- ANDROID: On first run, the app REQUESTS all needed runtime permissions itself
  (microphone; notifications; network) with clear Arabic rationale dialogs, and
  provides a single guided "Setup" screen for entering/validating API keys and
  toggling providers. No manual file editing by the user.
- PC (only for Path B): a SINGLE bootstrap script (`scripts/setup.ps1`) installs/
  starts the local bridge, checks for Tailscale + OpenCode, prints exactly what's
  missing with copy-paste fixes, starts the service, and shows a QR/pairing code.
  One command, then done.
- Ship `docs/SETUP.md` (Arabic + English) with the minimal steps.
- In the PERMISSIONS & PREP LIST (§0.6), enumerate every permission/account/change
  on BOTH devices and flag anything that cannot be automated.

## 12. MILESTONES (ship a compilable, working build at the END of EACH)

- M1 — Skeleton & Providers: scaffold, DI, Settings, secure key storage; AiProvider
  for Anthropic + OpenAI + DeepSeek (+ GitHub Models if available); a plain streaming
  chat screen to verify each provider end-to-end.
- M2 — Voice Capture: record audio; on-device transcription (+ whisper.cpp path stub);
  editable Arabic transcript; send to a chosen model.
- M3 — Clarify & Forge: the interactive analysis/dialogue engine (questions,
  suggestions, caveats) gated on the user's "idea is ready"; the framework library;
  the English-prompt generator. This is the core — invest the most here.
- M4 — Route, Execute, Translate, Feedback: rule-based router + transparent decisions
  + manual override + provider fallback; execute Path A; translate outputs to Arabic;
  spoken-feedback loop with full session context; optional AI-router toggle.
- M5 — History & Polish: Room-backed searchable history; re-run past tasks; optional
  TTS readback (opt-in, not core); complete loading/empty/error states; RELEASE APK.
- M6 — PC Agent Bridge (Path B): Tailscale-wrapped local server; OpenCode headless
  driver; the three guardrails (full change-report, approval gate, dir confinement);
  the one-command `setup.ps1` with QR pairing.

## 13. ACCEPTANCE CRITERIA

- Compiles and runs on a modern device/emulator at the end of every milestone.
- A standalone signed APK installs on a fresh Android device and runs Path A with
  only API keys entered — no dependency on the owner's PC.
- A second user can pair the app with THEIR OWN PC via the simple QR/code flow.
- User can speak Arabic, edit the transcript, be taken through a real clarifying
  dialogue, and only then get a generated English prompt.
- Outputs are shown translated into Arabic; spoken feedback refines the session.
- Router decisions are visible and overridable; provider fallback works on failure.
- Path A works fully with the PC off; Path B activates only when the PC is on, with
  all three guardrails enforced and a full change-report delivered.
- History persists across restarts and is searchable.
- Setup is one guided flow on Android and effectively one command on the PC.

## 14. WORKING DISCIPLINE

- Before each milestone: a short DESIGN NOTE (files, interfaces, data flow). Get it
  right, then implement.
- Small, compilable commits with clear messages; build (and test where possible)
  after each change. Provide a fake AiProvider for tests; unit-test the router and
  provider layers.
- Use your strongest reasoning for architecture/interfaces/routing; a faster model
  for boilerplate — but keep public interfaces consistent.
- Surface every assumption as a short note rather than guessing silently.

# ── NOW: run scripts/install-skills.sh, then produce the UNDERSTANDING BRIEF,
# ── QUESTIONS LIST, SUGGESTIONS LIST, and PERMISSIONS & PREP LIST from §0.
# ── Then STOP and wait for approval. Do not code yet.
