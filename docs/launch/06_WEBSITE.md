# L6 — The website

## Goal
One site that presents Braining with a modern, technical, **calm** interactivity — the owner's words:
*"عصري وتقني مميز ومتفاعل جداً بشكل هادئ."* It hosts the download, the guide (L3), the policies (L4)
and the contact, and it is the public privacy-policy URL Play needs.

## Hosting — decided by the product's own posture
**GitHub Pages** from the same repository (or a `braining-site` repo): free, no server to run or
secure, HTTPS, custom domain optional. A BYOK app that promises "no server" should not need one for
its website either. Anything requiring a backend (forms that store data, analytics with a vendor) is
out of scope here; contact goes to Telegram.

## Content (one long page plus policy pages)
1. **Hero**: name, one-line promise in Arabic and English, the download button (GitHub release now,
   Play badge after L5), and a quiet live demo — a typed Arabic prompt, a streamed answer — rendered
   in CSS/JS, no video autoplay.
2. **What it is**: voice-commanded, Arabic-first, six providers, your own keys, nothing leaves your
   phone. Three short paragraphs, not a feature grid.
3. **The providers**: the six logos-as-text (brand names only — no third-party logos redrawn),
   each linking to the guide section for its key.
4. **Clarify**: the twelve-question interrogation explained with an animated example of one question
   → one answer → the forged brief.
5. **Your PC, from anywhere**: Ollama + Tailscale, and (after M7) the agent bridge — with the three
   guardrails stated plainly. Trust is the selling point; say what it does *not* do.
6. **Privacy in one screen**: the same text as `PRIVACY.md`, verbatim.
7. **Download & guide**: release link, the two PDFs, the video (when recorded).
8. **Footer**: "© 2026 Moustafa Afara" → `https://t.me/Mustafa_Afara`, Terms, Privacy, Source.
9. `/privacy` and `/terms` as standalone pages — Play needs a direct URL, not an anchor.

## Design constraints
- **RTL-first, LTR second**, with a language toggle that swaps direction, not only strings.
- The app's own palette (`BrandPalette` — `Ink.Ground #0E0D14` dark, `Paper.Ground #F6F5FA`
  light), the app's Arabic typeface if licensed for web use; system fallback otherwise.
- **Calm interactivity**: scroll-triggered reveals, one streamed-text demo, hover states. No
  parallax storms, no autoplaying media, no cookie banner (there is nothing to consent to).
- Static, single `index.html` + CSS + JS, no framework needed; must score well on Lighthouse
  performance and accessibility; must read perfectly on a phone.
- No analytics by default. If the owner wants counts, a privacy-respecting, cookieless option only.

## Deliverables
`site/` in the repo (or the sibling repo): `index.html`, `privacy.html`, `terms.html`, assets, and
a GitHub Pages deployment; the public URLs recorded in `PROJECT_STATE.md` for L4/L5.

## Steps
1. Owner confirms domain (GitHub default `*.github.io`, or a custom domain he buys). 2. Build the
page from L3/L4 texts — do not invent copy. 3. Screenshots from the real app. 4. Deploy. 5. Verify
the privacy URL from a phone with no login.

## Acceptance
Loads in under two seconds on a mid-range phone; RTL and LTR both correct; every link resolves;
`privacy.html` is a stable URL usable in Play Console.

## What the owner must provide
Domain decision; approval of the copy (it is his voice); whether he wants any visitor counting at all.
