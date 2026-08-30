# OpenCode Persistent Instructions — Braining (فهم)

These rules apply to EVERY session while building this project. `BRAINING.md` is the
authoritative spec; this file keeps the working discipline in front of you at all times.

## Prime directive
**Analyze first, execute once.** "An hour of analysis saves days of execution."
Over-clarify before acting. Never under-clarify. Ask about anything unclear; offer
every useful suggestion; surface every risk and caveat BEFORE writing code.

## Before you start (once)
1. Read `BRAINING.md` and all files in `docs/`.
2. Run `bash scripts/install-skills.sh` to install agent skills.
3. Produce: Understanding Brief, Questions List, Suggestions List, Permissions & Prep
   List. Then STOP and wait for the owner's approval before Milestone 1.

## Every milestone
- Write a short DESIGN NOTE first (files, interfaces, data flow); get sign-off if unsure.
- Implement in small, compilable commits with clear messages.
- Build (and run tests where possible) after each change. Never leave the tree
  non-compiling.
- End each milestone with a working build. Show the diff and a brief summary.

## Architecture rules
- Clean Architecture + MVVM, multi-module (see `docs/ARCHITECTURE.md`).
- Adding a provider, an STT engine, or a prompt framework must be a SMALL, isolated
  change. Keep public interfaces stable.
- Preserve the full `Session` context object across the whole request lifecycle.

## Path A vs Path B
- Path A (API-only) is the self-sufficient core; it must work with the PC OFF.
- Build and validate ALL of Path A before starting Path B.
- Path B drives OpenCode headless inside a confined working directory, over Tailscale.

## Standalone APK
- No hardcoded keys, endpoints, IPs, or PC identity. Everything user-specific is
  entered at runtime and stored encrypted. Each user pairs with THEIR OWN PC.
- Deliver a release-signed APK; document the exact build command and output path.

## PC guardrails (Path B) — non-negotiable
1. Full change-report after every PC task (files + system/config changes + action log).
2. Approval gate before any destructive action.
3. Working-directory confinement unless the user explicitly widens it.

## Providers
- Claude, ChatGPT, DeepSeek, **Google Gemini**. NO Copilot provider (no general
  text-generation API). Claude is the default brain for Clarify/Forge/Translate.
- GitHub Models was replaced by Gemini per `ANSWERS.md` Part 1 §3 — it is closed to new
  sign-ups and survives only as a labelled, non-blocking stub in Settings. Do not build on it.
- Model names are NOT constants to be typed inline. They live in `ProviderId.defaultModel`,
  and the user can override each one in Settings. Vendors retire names on their own
  schedule: `gemini-2.0-flash` died 2026-06-01 and `deepseek-chat` died 2026-07-24, each
  time breaking the app silently. Check the vendor's deprecation page before trusting a name.
- Transparent routing: always show which model ran + one-line rationale; allow override
  and provider fallback.

## Security
- Keystore-encrypted keys; keys never logged, never in URLs, leave only to their
  provider. Bridge binds to the Tailscale IP only — never 0.0.0.0.

## When a skill slug 404s
Substitute the closest current equivalent from https://www.skills.sh, install it, and
note the substitution. Never block the build on a single moved slug.

## Communication
- Surface assumptions as short notes rather than guessing silently.
- Keep the owner in control: propose, don't presume, on anything irreversible.
