# L3 — The user guide (PDF) and the video script

## Goal
A single, comprehensive, generated PDF that takes a new user from install to every feature working —
**and** a video script that follows the same steps in the same order, so the owner can record it
himself and the two never disagree. The PDF is linked from the app (Settings → "دليل الاستخدام"), from
the GitHub release, and from the website (L6).

## Language
**Arabic first, English second** — the app itself is Arabic-first with an English toggle, and the
guide mirrors that: one PDF per language, identical structure, generated from one source so they
cannot drift.

## What already exists
- `ProviderGuide.kt` — per-provider key URL, docs URL, key prefix, free-tier and region flags.
  **The guide is generated from this**, not written by hand, so a new provider appears in the PDF
  the day it appears in the app.
- The key-guide screen in Settings (M5.3), Ollama setup (M5.2 + Part 16), OpenRouter (Part 17),
  Tailscale (Part 16), Deepgram (M2).
- `README.md`, `docs/SETUP.md` — developer-facing; the user guide is *not* these.

## Structure (both the PDF and the video follow it exactly)
1. What Braining is, in one page, and what BYOK means for the user (their key, their cost, their
   privacy — nothing leaves the phone).
2. Install: from the GitHub release now; from Google Play after L5. Enabling unknown sources.
3. First run: onboarding, the greeting, the language toggle.
4. **Getting a key, one section per provider**, in the app's provider order — where to click, what
   the key looks like, the free tier if any, region limits (Gemini), how to paste it, how to test it.
5. Ollama on your own PC: install, `OLLAMA_HOST`, the address to enter, the test button.
6. Reaching your PC from outside: Tailscale on both devices, the `.ts.net` name, the tunnel toggle.
7. Voice: on-device transcription; Deepgram as the optional upgrade and where its key comes from.
8. Chat: providers, switching, the fallback chooser, copy, **export as file / share** (M6).
9. Clarify (interrogation mode): what it is for, the twelve questions, forge, execute, save, re-run.
10. History and saved runs.
11. Reporting a problem (M8): the one tap, what the report contains, what it never contains.
12. FAQ and errors: every user-facing error string, what it means, what to do.
13. Privacy and terms in one page (from L4).

## Deliverables
1. `docs/guide/GUIDE_AR.md` and `GUIDE_EN.md` — the sources.
2. `Braining-Guide-AR.pdf`, `Braining-Guide-EN.pdf` — generated (use the `pdf` skill; RTL must be
   verified page by page — Arabic PDF rendering is the classic failure here).
3. `docs/guide/VIDEO_SCRIPT_AR.md` — scene by scene, same numbering as the PDF sections, with what
   is on screen and what the owner says. Target 12–15 minutes.
4. Screenshots for every step, taken from the real app on the owner's phone (an agent with device
   access can take them all with `screencap` in one session).
5. The in-app link (Settings row) and the release/website links.

## Steps
1. Freeze feature behaviour (after L2). 2. Write `GUIDE_AR.md` section by section from the sources
above. 3. Capture screenshots. 4. Generate the PDF, review RTL on every page. 5. Translate to EN.
6. Derive the video script from the final AR guide. 7. Wire the in-app link.

## Acceptance
A person who has never seen the app reaches a working chat with two providers, exports a file, and
sends a problem report using only the PDF. RTL renders correctly on every page. Video script and PDF
have identical section numbering.

## What the owner must provide
- Nothing to install. He records the video himself, later, from the script.
- A preference: **PDF hosted in the GitHub release as an asset, or on the website only?**
  *(Recommended: both — the release is where friends download today.)*
