# L7 — The complete technical narrative, in simple Arabic

## Goal

One document, **in Arabic, in the simplest technical language possible**, telling the whole story of
the project **from the first idea to the end of L7 itself** — every stage, nothing skipped — so the
owner can review what was done, understand it, and hand it to anyone.

His instruction, twice given: *"من أول خطوة لإعداد البيئة وحتى نهاية عمل المشروع كاملاً"*, then
widened on 2026-09-05: **"حتى يشمل كل مراحل التطبيق من الفكرة على أندرويد وحتى L7 تماماً"**.

**Scope, stated exactly so nobody narrows it later:** the idea → the Android decision → the
environment → the architecture → **M1 through M8, milestone by milestone** → **L1 through L6, phase
by phase** → and finally L7 itself, the document explaining how it was written. Fourteen parts. If a
stage happened in this project, it has a section here.

## Why it is written last

It documents everything before it, **including L1–L6**. Written earlier it would be wrong by the
time it is read. This is the one phase that cannot start until the phase before it has finished —
which is why it sits at position 7 of 9, after the website and before marketing.

## What already exists — the sources, all in English

`PROJECT_STATE.md` (§1–§11: state, hard constraints, modules, the 67 lessons, the change log),
`ANSWERS.md` (Parts 1–21 — every ruling the owner made and the reasoning that produced it),
`docs/HISTORY_2026-07_to_08.md` (the verbatim early change log, 57 entries), `BRAINING.md` (the
original brief), every `docs/M*.md`, and this whole `docs/launch/` folder.

**This document is a translation of the project into plain Arabic, not a new source of truth.**
Where it and `PROJECT_STATE.md` disagree, `PROJECT_STATE.md` wins and this is corrected.

## Structure — fourteen parts

### الجزء الأول · الفكرة والقرار
1. **الفكرة** — what Braining is and why it was built, in the owner's own words from `BRAINING.md`:
   an Arabic-first, voice-commanded orchestrator, and the goal that started it all — *"أن أطلب مهاماً
   من النموذج وأنا بعيد عن الحاسب."*
2. **لماذا أندرويد أولاً** — the choice and its consequences: Kotlin/Compose, the phone the owner
   actually carries, and why iOS is a separate build rather than a setting (L5 Part B).

### الجزء الثاني · الأساس
3. **تهيئة البيئة** — the exact toolchain and *why* each piece: JDK, Android SDK, Gradle 9,
   AGP, Kotlin 2.3, KSP, Compose BOM, Hilt, Ktor, Room. The three-adb problem and its standing rule
   (lesson 67). The "owner builds, agent edits" workflow and why it existed — and how it changed on
   2026-09-05 when the owner granted build/device autonomy.
4. **بنية المشروع** — the modules and what each owns, with Clean Architecture and MVVM explained in
   a paragraph each for a reader meeting them for the first time. One boxes-and-arrows diagram per
   module boundary.

### الجزء الثالث · مراحل البناء، مرحلةً مرحلة (M1 → M8)
5. **M1 → M4** — the foundations, chat, voice, Clarify, history. What was built, what broke, what
   was learned.
6. **M5** — six providers, the key guide, Ollama over the network, OpenRouter, Tailscale.
7. **M6** — file generation, export and sharing.
8. **M7** — the PC agent bridge: Tailscale, OpenCode headless, the three guardrails, the deny-list.
9. **M8** — field diagnostics: the `Diag` tag, the crash handler, the report and its aggregate index.

   **Told as stories, because that is what makes them stick:** the Gemini em-dash that silently
   corrupted a key; the IPv6 hole where `fd00::1.evil.com` passed the private-address check; the git
   accident that recorded the deletion of an entire milestone; the provider menu fixed three times
   before the real cause was found; and the black screen — four hours, three captures, an app that
   logged nothing, and a fault never explained.

### الجزء الرابع · الأمان والخصوصية
10. **الأمان والخصوصية** — BYOK and what it means for the user; the Android Keystore;
    `LocalEndpoint` as the single cleartext guard and why it is a release blocker; why the router
    port is never opened; the M7 guardrails and deny-list; `allowBackup=false` and its deliberate
    consequence.

### الجزء الخامس · مراحل الإطلاق، مرحلةً مرحلة (L1 → L6)
11. **L1 · الدعم والاختبار** — why remote access to users' phones was **refused** and what replaced
    it; the automated suites, the device-driven loops, the one manual matrix, and the protocol that
    turns a user's complaint into a fix.
12. **L2 · التدقيق الأمني** — the threat model, the repeatable audit, the bridge penetration
    checklist, and what "secure" honestly means (and does not mean).
13. **L3 → L6** — the guide and video; the license, terms, privacy policy and the ownership screen;
    the Google Play submission **as it actually happened**, step by step, with the Apple assessment;
    and the website.

### الجزء السادس · الخاتمة
14. **هذه الوثيقة نفسها (L7)** — how it was assembled and from which sources, so the next reader can
    update it rather than rewrite it. Then **ما لم نبنِه ولماذا** — the deliberate deferrals from
    §9, so nobody reads them as gaps. Then **قاموس** — every technical term used, one line each,
    Arabic with the English in brackets.

## Style rules

Short sentences. One idea per paragraph. Every acronym expanded on first use. Diagrams as simple
boxes and arrows (text or SVG). **The test is a single question: is there a sentence in it the owner
would have to ask about?** If yes, it is rewritten, not footnoted.

## Deliverables

- `docs/TECHNICAL_STORY_AR.md` — the source.
- `Braining-Technical-Story-AR.pdf` — generated, **RTL verified page by page** (Arabic PDF rendering
  is where this fails; check every page, not a sample).
- A two-page English executive summary for anyone who reads only that.

## Acceptance

The owner reads it end to end and can explain any part of the app to a friend without opening the
code. **Every stage from the idea to L6 has a section** — the completeness check is mechanical: walk
`PROJECT_STATE.md` §7 and this folder, and confirm each milestone and phase appears. Every claim
traces to a `PROJECT_STATE.md` section or an `ANSWERS.md` part.

## What the owner must provide

His review, section by section, and a list of anything he still did not understand — **each of those
is a rewrite, not a footnote.**
