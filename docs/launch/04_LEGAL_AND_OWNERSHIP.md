# L4 — License, terms, privacy policy, and ownership

## Goal
Terms and licensing that establish **exclusive ownership under the name Moustafa Afara**, an About
screen where that name is tappable and opens `https://t.me/Mustafa_Afara`, and — the item the owner
did not name but Google Play will demand — a **privacy policy at a public URL**.

**A caveat that must stay attached to this brief:** the agent writing these documents is not a
lawyer. It can produce clear, standard, well-structured texts; the owner should have them read by a
lawyer in his jurisdiction before the Play submission. Nothing here is legal advice.

## Why it is here
- Play Console refuses a submission without a privacy-policy URL and a completed Data Safety form.
- A BYOK app has an unusual and important thing to say in its terms: **the user's keys, the user's
  costs, the user's responsibility to the provider's own terms.** Silence there is a liability.
- "Exclusive ownership" and "public GitHub repository" are compatible — but only if the license says
  so explicitly. A repository with no license is *legally* all-rights-reserved, but nobody reading it
  knows that, and Play reviewers, users and copiers will assume the opposite.

## The decision that shapes everything — **RULED 2026-09-05: public, source-available, all rights reserved**
**Public source-available, or private repository?**
- **Public, all rights reserved (source-available):** the code stays visible (the share button and
  the release already live on GitHub), the license says explicitly: you may read, build for
  personal use, and report issues; you may not redistribute, modify for distribution, sell, or
  remove attribution. This is *not* open source, and must not use an OSI license name.
- **Private:** simpler legally, but the download link then needs a separate public repo or the
  website to host releases.
*(Recommended: public source-available — it matches the trust-through-transparency posture of a BYOK
app, and the owner's friends can see that nothing leaves their phone.)*

## Deliverables
1. **`LICENSE`** — proprietary, source-available, in English with an Arabic summary; copyright
   "© 2026 Moustafa Afara. All rights reserved." Explicit permitted/forbidden lists as above.
2. **`TERMS.md` / in-app «الشروط»** — plain language, both languages. Must cover: BYOK (keys and
   costs are the user's; the user is bound by each provider's terms); no warranty; the app sends
   prompts only to the provider the user chose and to the user's own PC if configured; the PC bridge
   executes tasks *on the owner's own machine only* (never a user's); age (13+/16+ per region);
   changes to terms.
3. **`PRIVACY.md` / in-app «الخصوصية»** — the honest and unusually short one: no server, no
   analytics, no account, nothing collected; keys stored in the device Keystore; prompts go to the
   provider chosen; voice audio is never stored; diagnostic reports (M8) are generated locally and
   sent **only** when the user chooses, contain an anonymous random id and no location; file exports
   go where the user sends them. Hosted at a public URL (GitHub Pages via L6, or the repo) — Play
   needs the URL.
4. **About screen** (`feature-settings`): app name, version, "© Moustafa Afara" as a **tappable
   link** opening `https://t.me/Mustafa_Afara` (`Intent.ACTION_VIEW`, falls back to a browser if
   Telegram is absent), links to Terms, Privacy, the Guide (L3), and the source.
   **Two Telegram links, and they are not interchangeable:** the owner's name goes to his *personal*
   account (`Mustafa_Afara` — his ruling, and the URL keeps that spelling); the app's official
   channel is **`Braining_AI`** (https://t.me/Braining_AI) and appears as
   "القناة الرسمية / Official channel". A user wanting updates should never have to message the owner
   directly to get them.
5. **Attribution kept in the APK**: the About text is a string resource, not stripped by R8.

## Steps
1. Owner rules on public/private. 2. Draft the three texts (EN + AR). 3. Owner reviews; lawyer
review recommended. 4. Build the About screen and the two in-app pages (Markdown rendered in-app, or
opened as web pages). 5. Host `PRIVACY` publicly and record the URL in `PROJECT_STATE.md` — L5 needs
it.

## Acceptance
The repo shows a license; the app shows terms, privacy and the tappable name; the privacy URL loads
from a phone with no login; Play's Data Safety answers can be filled in truthfully from `PRIVACY.md`
alone.

## What the owner must provide
- ~~The public/private ruling~~ taken: public. - Name **confirmed: "Moustafa Afara"** (as in the
  email). - The minimum age he wants to state — still open.
