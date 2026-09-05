# M6 — File generation & sharing

**Status:** specified 2026-09-05, not started. Blocked behind the black screen (§8) only in
sequence, not in code — nothing here touches `ChatScreen`'s top bar or the start-up path.

**Owner instruction that defines this milestone (2026-09-05):** the models used in the app should
be able to *generate files* in **both** normal chat and Clarify (interrogation) mode, and the user
should be able to **download** those files and **share** them over social apps. Keep the work
simple — ideally one instruction to the model, all requirements in one batch, and one flat test
list.

This note is written so that starting M6 is exactly that: one batch, one test list.

---

## 1. The idea in one sentence

The model already produces text — a chat answer, or the forged brief at the end of an
interrogation. **M6 turns any such output into a real file the phone can save and share through the
normal Android share sheet.** The model writes the *content*; the app owns the *file*. No new model
capability, no function-calling, no tool schema — which is the whole reason M6 is small.

**What this is not.** It is not the model reaching into the filesystem, and it is not a document
engine. "The model generates a file" means the model writes clean text and the app packages it. A
PDF/Word renderer is a separate, heavier increment (§6) and is deliberately out of v1.

## 2. The one instruction to the model

A **single system-prompt line**, added only when the user's request is file-shaped ("اكتب لي
تقريراً / ملف / جدول / وثيقة"): tell the model to answer in **clean, self-contained Markdown whose
first line is a `#` title.** That is the entire model-side change.

Markdown because it is plain text — no library, nothing to leak, opens in every app — and it
already carries headings, lists, tables and code fences, so "generate a table" needs no special
handling. Chat today sends **no** system prompt (`PROJECT_STATE.md` §8), so this line is added
per-request, not globally, and only on the file-shaped path. Whether Clarify gets the same nudge is
an owner decision (§5).

## 3. What the user sees

- **Chat mode:** an *export / share* icon on each assistant bubble, in the same row as the existing
  copy button (`MessageBubble`, `ChatScreen.kt`). One tap → the share sheet; a second action saves
  to a location the user picks.
- **Clarify mode:** the same control on the final forged output.

Two entry points, one helper behind both. Per-message and Clarify-result only in v1 — a
"whole conversation" export is listed as an owner decision (§5) because plain chat is not recorded
(`PROJECT_STATE.md` §8), so "the conversation" only ever means what is currently on screen.

## 4. The mechanics (all in `core-ui`, shared by both features)

A single `FileExport` helper — chat and Clarify are siblings, so anything both need lives in
`core-ui`, never in a peer (hard constraint 8):

1. **Write** the text to a dedicated app-internal dir (`cacheDir/exports/`), always UTF-8.
2. **Expose** it through a `FileProvider` (`androidx.core`) as a `content://` URI with a temporary
   read grant. *Not present today* — M6 adds the `<provider>` to the app manifest and a
   `res/xml/file_paths.xml` scoped to that one dir. Nothing else on disk becomes reachable.
3. **Share:** `Intent.ACTION_SEND`, MIME `text/markdown` (fallback `text/plain`), `EXTRA_STREAM` =
   the URI, wrapped in `Intent.createChooser`. This is the "social apps" path — WhatsApp, Telegram,
   Gmail, Drive all accept it.
4. **Download:** `Intent.ACTION_CREATE_DOCUMENT` (Storage Access Framework). The user picks where
   it lands. **No storage permission**, works on every supported API (min 26). This is the
   "download" path.
5. **Filename:** derived from the first `#` heading / first line, sanitised (`/ \ : * ? " < > |`
   stripped, whitespace collapsed, length-capped), Arabic kept, timestamp fallback when the text
   has no usable first line.

## 5. What the owner must decide before the batch starts

Nothing to **install** — no new SDK, no account, no key. The decisions are all product choices, and
each has a recommended default so silence is a safe answer:

1. **Format for v1:** Markdown only *(recommended)*, or also plain `.txt`? PDF/Word → §6, later.
2. **Download behaviour:** system "Save to…" picker *(recommended — no permission, any location)*,
   or auto-save straight to the Downloads folder?
3. **Scope:** per-message + Clarify result *(recommended)*, or also "export the whole current
   chat"?
4. **Model nudge:** add the one Markdown system-prompt line when the request is file-shaped
   *(recommended yes)* — in chat only, or Clarify too?
5. **Filename:** derive from the title *(recommended)*, always timestamp, or ask every time?

## 6. Deliberately deferred (so nobody reads it as missed)

- **PDF / Word export.** Needs a rendering library and real layout testing across Arabic RTL, page
  breaks and fonts — its own milestone, and exactly the kind of test surface this batch is meant to
  avoid. Markdown ships first; if the owner wants PDF, it is added on top without redoing v1.
- **Whole-conversation / transcript export** while chat remains unrecorded.
- **Model-authored binary files** (images, spreadsheets). Out of scope; the model writes text.

## 7. Security — release checks, not preferences

- The file contains **only** the visible assistant text. Never a key, never a request body, never
  diagnostics. BYOK is unaffected: this feature reads rendered message content and nothing from the
  key store (hard constraint 3).
- `FileProvider` paths are scoped to `cacheDir/exports/` alone. No broad storage export, no
  `content://` to anything else.
- Exported files inherit the same "no raw audio, ever" rule — there is none in a text export, and
  there is no path that could introduce one.

## 8. The one test list (run in order, on the owner's phone)

1. Export a chat answer → the share sheet opens → send to WhatsApp/Telegram → the file arrives and
   opens with its text intact.
2. Export the same answer → "Save to…" → the file is written where chosen and its content matches.
3. Export the final Clarify output → both share and save behave as (1) and (2).
4. **Arabic content** exports as UTF-8 and renders right-to-left in the receiving app; a mixed
   Arabic/Latin/`code` answer keeps all three.
5. A long, multi-section answer (headings, a list, a table, a code block) exports **in full** — no
   truncation, structure preserved.
6. The exported file contains no API key and no request JSON — open it and read it.
7. A title with slashes/emoji/newlines produces a safe filename and still saves.

Seven rows, one surface. If any needs the app rebuilt more than once, the batch was split wrong.

## 9. Dependency note for M8

M8 (field diagnostics) reuses this exact `FileExport` helper to hand the user a diagnostic file.
Build M6 first and M8 inherits the save/share plumbing for free. See
`docs/M8_FIELD_DIAGNOSTICS.md`.
