# Visual Identity — Braining (فهم)

The design spec for the app. Treat this as an amendment to `BRAINING.md`: it governs
all user-facing visuals. Logo source files are in `assets/logo/`.

> **Revised 2026-08-18 — the «مِداد» identity.** The owner reviewed three directions and adopted
> this one: *the ground goes neutral, and the colour moves to what is touched.* §2 (colour),
> §5 (shape and motion) and §5b (buttons) are rewritten. §1, §3, §4 and §6 stand as they were,
> with two marked notes. The rulings are recorded in `ANSWERS.md` Part 10.
>
> **The diagnosis this revision answers, because it explains every change below:** the previous
> palette used a saturated indigo as the *ground*. The eye reads chroma multiplied by area, so a
> saturated colour spread across a whole screen exhausts attention and leaves the accent nothing
> to win against — every control was competing with its own background. The fix is not a nicer
> violet. It is putting the violet where it is read.

---

## 1. The mark

Five vertical bars rise toward a tall centre bar, with a single dot floating above it.

**Meaning:** many scattered voice inputs converge into one clear understanding. The
dot is simultaneously the nuqta of ف in «فهم» and the moment of insight. Do not
redraw, restyle, or "improve" the mark — use the supplied SVGs.

**Files:**
| File | Use |
|---|---|
| `assets/logo/icon.svg` | Full icon with indigo background. Splash, about screen, store listing. |
| `assets/logo/icon-foreground.svg` | Adaptive-icon foreground layer, safe-zone compliant. |
| `assets/logo/icon-mono.svg` | Monochrome, uses `currentColor`. Notifications, themed icons. |

> **Note, 2026-08-18.** The mark's *form* is still fixed and must not be redrawn. Its two colours
> now follow the theme (`primary` for the bars, `tertiary` for the centre and the dot). With a
> near-black dark theme and a white light theme, one fixed violet would glare on one and wash out
> on the other. The rule protects the shape; the hues follow the surface they sit on.

**Clear space:** never place other elements closer than 12% of the icon's width.
**Never:** stretch, rotate, recolour outside the palette, add gradients, glows, or
shadows, or place the mark on a busy background.

---

## 2. Colour — «مِداد»

**Dark is the app's home; light is fully supported and never an afterthought.**

### Dark

| Token | Hex | Role |
|---|---|---|
| `Ink.Ground` | `#0E0D14` | The page behind everything. Near-black, faint violet bias |
| `Ink.Surface` | `#17161F` | Cards, sheets, the input dock — one step up |
| `Ink.Raised` | `#201E2B` | Pressed and hovered surfaces, chips |
| `Ink.Line` | `#2C2937` | Hairline borders — edges are drawn with a line, not a shadow |
| `Ink.Text` | `#E8E6F0` | Body and headings |
| `Ink.Muted` | `#9B97AC` | Labels, captions, anything secondary |
| `Ink.Accent` | `#8B84F7` | **The interactive colour.** Buttons, active states, the mark |
| `Ink.OnAccent` | `#0B0A11` | Drawn on top of the accent |
| `Ink.AccentSoft` | `#241F45` | Tonal button fill, the user's own chat bubble |
| `Ink.Amber` | `#F0A500` | **Insight accent** — centre bar, dot, «نضجت الفكرة» |
| `Ink.AmberSoft` | `#3A2A05` | A tonal amber container, rarely |

### Light

| Token | Hex | Role |
|---|---|---|
| `Paper.Ground` | `#F6F5FA` | The page |
| `Paper.Surface` | `#FFFFFF` | Cards |
| `Paper.Raised` | `#EFEEF6` | Tinted surfaces |
| `Paper.Line` | `#E0DEEC` | Borders |
| `Paper.Text` | `#1A1826` | Body |
| `Paper.Muted` | `#6A667C` | Secondary |
| `Paper.Accent` | `#5B51D8` | Interactive — **darker than the dark theme's**, because it carries contrast on white |
| `Paper.AccentSoft` | `#E8E6FB` | Tonal fill |
| `Paper.Amber` | `#B26B00` | Insight |
| `Paper.AmberSoft` | `#FBF0DC` | Tonal amber |

**Amber is scarce by rule, and the rule now has teeth.** It marks the moment of understanding:
«نضجت الفكرة», the recording indicator, the mark's centre. There is exactly one amber *button* in
the app — `InsightButton` — and a second use of it anywhere is the overuse this rule forbids.

Semantic: success `#1D9E75`, warning `#BA7517`, error `#E24B4A`. The four derived error tones
(`ErrorLight` `#EC9393`, `ErrorDark` `#4A1C1C`, `ErrorPale` `#FBE4E4`, `ErrorDeep` `#631D1D`) were
ratified in `ANSWERS.md` Part 6 §M2-5 and **carry over unchanged**: they clear 4.5:1 by a wider
margin on the darker ground than they did on the old one.

**No colour literal may be written outside `core-ui/theme/Color.kt`.** Screens read
`MaterialTheme.colorScheme`; nothing reads the palette object directly.

## 3. Typography

- **Arabic (primary):** IBM Plex Sans Arabic, or Noto Sans Arabic as fallback.
  Bundle the font — do not rely on the device default, which varies wildly across
  Android skins and will break the layout on Xiaomi/Samsung devices.
- **Latin:** the same family's Latin cut, or Inter.
- **Weights: 400 and 500 only.** Never 600/700 — heavy weights read poorly in Arabic.
- **Line height 1.7** for Arabic body text; Arabic needs more leading than Latin.
- Material 3 type scale, sentence case everywhere. Never ALL CAPS — it is meaningless
  in Arabic and looks broken in mixed text.

---

## 4. RTL is the default, not an afterthought

- Default layout direction is **RTL**. English is the toggled alternative.
- Use `start`/`end` padding and alignment — **never** `left`/`right`.
- Mirror all directional icons (back arrows, chevrons, send) under RTL.
- Numerals: Western Arabic digits (0–9) by default.
- Test every screen in both directions before calling a milestone done.

---

## 5. Shape, spacing, motion

- **Corner radius — raised 2026-08-18.** 16dp buttons · 20dp cards · 10dp text fields ·
  28dp sheets and the recording panel. A tight radius is the single detail that dates an
  interface fastest: it is a leftover from interfaces imitating physical buttons, and a modern
  control is treated as a touch area. The five Material slots live in `core-ui/theme/Shape.kt`;
  the button radius is set by the wrappers, because Material's own default is a full pill and a
  pill reads as a tag rather than a control.
- Spacing scale: 4, 8, 12, 16, 24, 32dp. Generous whitespace; this is a thinking tool.
- **Elevation: flat surfaces with hairline borders, and exactly one exception** — a soft shadow
  under the recording panel, so its height off the page is readable while the conversation
  scrolls behind it. Nothing else casts a shadow. **No gradients anywhere.**
- Motion: 150ms for state changes, 250ms for transitions, standard easing. The recording
  indicator may pulse; nothing else animates continuously.
- **A press must be answered.** Every button shrinks to 96% on a spring while held. Material's
  ripple alone is a colour change and is nearly invisible on a dark ground; the scale is what
  makes a control feel like a control. This is the other half of the owner's 2026-08-18 report.

## 5b. Buttons — four weights, and the hierarchy is the point

Defined once in `core-ui/components/BrainingButtons.kt`. **Do not call Material's `Button`,
`OutlinedButton` or `FilledTonalButton` directly** — a shape passed by hand at twenty call sites
is a shape that will be right at nineteen of them.

| Weight | Use | Rule |
|---|---|---|
| `PrimaryButton` | The one action the screen exists for | **One per screen** |
| `TonalButton` | A real action that repeats and must not shout — regenerate, swap | Any number |
| `QuietButton` | An alternative or a way out — cancel, an option among several | Any number |
| `InsightButton` | Amber. «نضجت الفكرة» | **Exactly one, in the whole app** |

`TextButton` stays Material's, for the lightest actions — copy, undo, clear. It has no container,
so it carries no shape decision.

## 6. Screen-specific identity

> **Note, 2026-08-18.** The recording panel is docked at the foot of the screen and is **not** a
> modal sheet: the conversation above it stays readable, scrollable and touchable while dictation
> runs. See the KDoc on `VoiceCapturePanel`.

- **Voice capture:** the mark's waveform is the live audio visualiser — the bars react
  to real input amplitude. This is the signature interaction of the app; get it right.
- **Clarify dialogue:** distinguish speakers clearly — the user's turns on one side,
  the system's questions/suggestions/caveats on the other, each visually distinct.
  Caveats and warnings carry the warning colour, never amber.
- **Forge:** show the generated English prompt in monospace, LTR, inside a clearly
  bounded container even while the surrounding UI is RTL.
- **Results:** Arabic translation is the primary view; the English original is
  available but secondary.

---

## 7. Instruction to the building agent

Apply this identity across the app now, and keep it applied in every later milestone:

1. Add the three SVGs from `assets/logo/` to the project. Generate the Android
   launcher icon from `icon.svg`, and wire an **adaptive icon** using
   `icon-foreground.svg` as the foreground over an `#26215C` background layer.
   Include the monochrome layer for themed icons. Produce all required densities.
2. Define the full colour palette above as a Material 3 `ColorScheme` in `:core-ui`,
   with complete dark **and** light variants. No hardcoded colours anywhere else in
   the codebase — every colour must come from the theme.
3. Bundle the Arabic font and wire the Material 3 type scale with weights 400/500 and
   1.7 line height for body text.
4. Set RTL as the default direction. Audit every existing screen for `start`/`end`
   usage and mirrored icons.
5. Replace the placeholder app name and icon with the Braining identity across the
   manifest, launcher, splash, and about screen.
6. Report which files you changed and show a screenshot or description of the launcher
   icon result at 48dp, 96dp, and in monochrome.

Do not invent additional brand colours, alternate logos, or decorative illustrations.
If a screen needs something not covered here, ask before designing it.
