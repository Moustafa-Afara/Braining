# L7 — The complete technical narrative, in simple Arabic

## Goal
One document, **in Arabic, in the simplest technical language possible**, that tells the whole story
of the project from the first environment setup to publication on the stores — so the owner can
review everything that was done, understand it, and hand it to anyone. His instruction: *"دقّق على أمر
أن يكون شاملاً لكل شيء بلغة تقنية بسيطة جداً."*

## Why it is last among the build phases
It documents everything before it. Written earlier, it would be wrong by the time it is read.

## What already exists — the sources, all in English
`PROJECT_STATE.md` (§1–§11: state, constraints, modules, lessons, change log), `ANSWERS.md`
(Parts 1–21, every ruling and why), `docs/HISTORY_2026-07_to_08.md` (the verbatim early change
log), `BRAINING.md` (the original brief), every `docs/M*.md`, and this `launch/` folder.
**This document is a translation of the project into plain Arabic, not a new source of truth** —
where it and `PROJECT_STATE.md` disagree, `PROJECT_STATE.md` wins and this is corrected.

## Structure
1. **الفكرة** — what Braining is and why it was built (the owner's own words from `BRAINING.md`).
2. **تهيئة البيئة** — the exact toolchain and why each piece: JDK, Android SDK, Gradle 9, AGP, Kotlin,
   Compose, Hilt, Ktor, Room; the three-adb problem and its rule; the "owner builds" workflow.
3. **بنية المشروع** — the modules and what each owns, with a one-paragraph explanation of Clean
   Architecture and MVVM for a reader who has not met them.
4. **المراحل M1 → M8، مرحلةً مرحلة** — what was built, what broke, what was learned. The §10 lessons
   are the backbone: each told as a short story (the em-dash key, the IPv6 hole, the git accident, the
   black screen).
5. **الأمان والخصوصية** — BYOK, the Keystore, `LocalEndpoint`, the cleartext rule, why the router
   port is never opened, the M7 guardrails and deny-list, the audit (L2).
6. **الاختبار** — how the app is tested: unit, instrumented, the device-driven loops, the manual
   matrix, the complaint protocol (L1).
7. **الإطلاق** — GitHub release, Play Console step by step as actually done (L5), the website (L6),
   the guide and video (L3), license and policies (L4).
8. **ما لم نبنِه ولماذا** — the deliberate deferrals (§9), so nobody reads them as gaps.
9. **قاموس** — every technical term used, in one line each, Arabic with the English in brackets.

## Style rules
Short sentences. One idea per paragraph. Every acronym expanded on first use. Diagrams as simple
boxes-and-arrows (text or SVG), one per module boundary. **No sentence the owner would need to ask
about** — that is the test.

## Deliverables
`docs/TECHNICAL_STORY_AR.md` and a generated PDF (`Braining-Technical-Story-AR.pdf`, RTL verified),
plus a two-page English executive summary for anyone who reads only that.

## Acceptance
The owner reads it end to end and can explain any part of the app to a friend without opening the
code. Every claim traces to a `PROJECT_STATE.md` section or an `ANSWERS.md` part.

## What the owner must provide
His review, section by section, and the list of anything he still did not understand — each of
those is a rewrite, not a footnote.
