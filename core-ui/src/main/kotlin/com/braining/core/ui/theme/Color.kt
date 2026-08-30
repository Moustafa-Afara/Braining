package com.braining.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The **مِداد (Ink)** palette — adopted by the owner on 2026-08-18, replacing the indigo identity.
 *
 * **This object is the only place a colour literal may be written in this app.** `Theme.kt` builds
 * both Material 3 `ColorScheme`s out of these tokens; screens read `MaterialTheme.colorScheme` and
 * never this object.
 *
 * ## What changed, and why it was the whole complaint
 *
 * The old palette used `#26215C` — a saturated indigo — as the **ground**: the colour behind every
 * screen. The owner's report was that the app looked heavy and that nothing stood out. Both follow
 * from the same fact: the eye reads chroma multiplied by area, so a saturated colour spread over a
 * whole screen exhausts attention and leaves nothing for the accent to win against. Every control
 * was competing with its own background.
 *
 * **مِداد inverts that relationship.** The ground becomes near-black with only a faint violet bias
 * — enough that the app is still recognisably itself, not enough to compete — and the violet moves
 * to the things that are *touched*: buttons, links, active states, the mark. The identity is not
 * weaker; it is spent where it is read.
 *
 * Amber is unchanged in role and stays scarce (BRAND §2): it marks the moment of understanding.
 */
object BrandPalette {

    /** The dark theme — the app's home. */
    object Ink {
        /** The page behind everything. Near-black, faint violet bias. */
        val Ground = Color(0xFF0E0D14)

        /** Cards, sheets, the input dock — one step up from the ground. */
        val Surface = Color(0xFF17161F)

        /** Pressed and hovered surfaces, chips, the second step up. */
        val Raised = Color(0xFF201E2B)

        /** Hairline borders. BRAND §5 draws edges with a line, not a shadow. */
        val Line = Color(0xFF2C2937)

        /** Body text and headings. */
        val Text = Color(0xFFE8E6F0)

        /** Labels, captions, anything secondary. */
        val Muted = Color(0xFF9B97AC)

        /** **The interactive colour.** Buttons, active states, the mark's outer bars. */
        val Accent = Color(0xFF8B84F7)

        /** Text and icons drawn on top of [Accent]. */
        val OnAccent = Color(0xFF0B0A11)

        /** The tonal button's fill, and the user's own chat bubble. */
        val AccentSoft = Color(0xFF241F45)

        /** The insight accent — the centre bar, the dot, «نضجت الفكرة». */
        val Amber = Color(0xFFF0A500)

        /** A tonal amber fill for the rare case amber needs a container. */
        val AmberSoft = Color(0xFF3A2A05)
    }

    /** The light theme — fully supported, never an afterthought (BRAND §2). */
    object Paper {
        val Ground = Color(0xFFF6F5FA)
        val Surface = Color(0xFFFFFFFF)
        val Raised = Color(0xFFEFEEF6)
        val Line = Color(0xFFE0DEEC)
        val Text = Color(0xFF1A1826)
        val Muted = Color(0xFF6A667C)

        /** Darker than the dark theme's accent, because it must carry contrast **on white**. */
        val Accent = Color(0xFF5B51D8)
        val OnAccent = Color(0xFFFFFFFF)
        val AccentSoft = Color(0xFFE8E6FB)
        val Amber = Color(0xFFB26B00)
        val AmberSoft = Color(0xFFFBF0DC)
    }

    /** Text drawn on an amber fill, both themes. Amber is light enough to need dark ink. */
    val OnAmber = Color(0xFF221703)

    val Success = Color(0xFF1D9E75)
    val Warning = Color(0xFFBA7517)
    val Error = Color(0xFFE24B4A)

    // ---------------------------------------------------------------------------------------
    // Derived error tones — ratified by the owner in `ANSWERS.md` Part 6 §M2-5 and **kept
    // unchanged through the مِداد redesign**. They are tonal steps on the same hue as [Error]:
    // lightness and chroma move, hue does not. They were measured against the old indigo ground
    // and clear 4.5:1 by a wider margin on the darker مِداد ground, so the ruling still holds
    // without re-derivation.
    // ---------------------------------------------------------------------------------------

    /** Error text and icons on dark surfaces. */
    val ErrorLight = Color(0xFFEC9393)

    /** Error card fill on the dark theme. */
    val ErrorDark = Color(0xFF4A1C1C)

    /** Error card fill on the light theme. */
    val ErrorPale = Color(0xFFFBE4E4)

    /** Error text on [ErrorPale]. */
    val ErrorDeep = Color(0xFF631D1D)
}
