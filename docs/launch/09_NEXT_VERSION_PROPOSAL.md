# L9 — Version 2: a proposal to return to later

**Status: proposal only.** Nothing here is committed. It exists so that when Version 1 has shipped
and settled, the conversation about what comes next starts from a page rather than from nothing.

## Principles that carry over unchanged
BYOK, no server, nothing leaves the phone without the user's tap, Arabic-first, the owner builds and
approves. Any V2 idea that needs a backend or an account should be treated as a different product.

## Candidate ideas, grouped, each with the reason it is worth considering

### Make the existing brains smarter about each other
- **Compare mode** — one prompt, two providers side by side, the user picks. Costs two keys' worth of
  tokens; the user chose that. The fallback machinery already knows how to run a second provider.
- **Cost meter per provider** — the token usage bar already exists; V2 prices it (public rate cards,
  editable) so the user sees dirhams/dollars, not tokens.
- **Local-first routing** — "use my Ollama when reachable, else fall back to X", automatically. The
  probe and the fallback list exist; this is a policy on top.

### Clarify, deeper
- **Saved interrogation templates** — the twelve questions are fixed today; a user could keep
  variants (a research brief, a business plan, a lesson plan).
- **Resume an interrupted interrogation** — explicitly deferred from M5 (`ANSWERS.md` Part 7 §M3-4);
  the highest-value single fix in Clarify.
- **Clarify from a file** — feed an exported Markdown (M6) back in as the idea.

### The PC, further
- **Task library** — named, re-runnable PC tasks with their own approval profile.
- **Whisper on the PC** — the free, accurate Arabic transcription `BRAINING.md` §9 already names;
  routed over the same Tailscale link the bridge uses.
- **Scheduled tasks** — "every morning, summarise this folder"; needs the auto-start decision from
  M7 to be settled first.

### Reach
- **iOS** — only per L5 Part B's assessment, gated on Play traction.
- **Tablet / foldable layouts** — Compose makes this cheap; the chat and Clarify screens want a
  two-pane layout on wide screens.
- **Wear / Auto voice entry** — say the idea, get the answer read back; the voice pipeline exists.

### Quality of life
- Themes beyond dark/light; font size; a widget for one-tap voice capture; export whole
  conversations once chat is recorded (a ruling is needed — chat is deliberately unrecorded today).

## What should be measured in V1 before choosing
Which providers users actually configure; how often the fallback chooser fires and what they pick;
Clarify completion rate (started vs forged vs executed); export usage; M8 report volume by error
type. All of it from Play stats and the M8 aggregate index — never from the app phoning home.

## How to turn this into a plan, when the time comes
Pick at most three items. Write each as an `M*` design note in the existing shape. Re-read §9 of
`PROJECT_STATE.md` first — some of these may already be half-built by then.
