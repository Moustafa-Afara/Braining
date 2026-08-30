package com.braining.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.braining.core.ui.R

/**
 * The Material 3 type scale under `docs/BRAND.md` §3.
 *
 * Three of BRAND's four typography rules are enforced here and now:
 *
 * - **Weights 400 and 500 only.** Never 600/700 — BRAND is explicit that heavy weights read
 *   poorly in Arabic. The Material defaults use Bold in the display and headline styles, so
 *   every style below is restated rather than inherited.
 * - **Line height 1.7 on body text.** Arabic needs more leading than Latin. Applied to the
 *   three body styles; titles and labels keep tighter ratios because they rarely wrap.
 * - **Sentence case, never ALL CAPS** — that is a call-site rule, not a `TextStyle` one, but
 *   it belongs with these: capitals are meaningless in Arabic and look broken in mixed text.
 *
 * - **IBM Plex Sans Arabic, bundled.** BRAND §3 warns that relying on the device font "will
 *   break the layout on Xiaomi/Samsung devices", and the test device is a Xiaomi. The two
 *   weights ship in `core-ui/src/main/res/font/`; the SIL OFL licence travels with them, at
 *   `licenses/IBM-Plex-Sans-Arabic-OFL.txt`.
 *
 * ## The fonts live in `core-ui`, not in `app` — and that is not arbitrary
 *
 * They were first placed in `app/src/main/res/font/`, on an instruction written in this very
 * file. That was wrong for the same reason `2026-07-29-C` moved the screen strings out of
 * `app`: **a library module cannot see the app module's `R` class.** This file is compiled
 * into `core-ui`, so `R.font.…` here resolves against `com.braining.core.ui.R`. A font sitting
 * in the app module's resources is invisible to it — the build fails with an unresolved
 * reference, and the tempting "fix" is to scatter font loading into the app module, which
 * would put typography in a different place from the palette that goes with it.
 *
 * Resource-name rules that bite here: files under `res/` may contain only lowercase letters,
 * digits and underscores, and `res/font/` accepts fonts only. `OFL.txt` alongside them fails
 * AAPT2 on both counts, which is why the licence lives outside `res/`. It still owes an in-app
 * attribution — an About screen in M5 — because the APK is meant to be shared (`ANSWERS.md`
 * Part 3) and OFL requires the notice to travel with the binary.
 *
 * Every style below routes through [BrainingFontFamily], which is the whole reason the scale
 * is written out longhand instead of being left to Material's defaults: the family is named
 * once. Do not load fonts at call sites.
 */
private val BrainingFontFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic_medium, FontWeight.Medium),
)

/** BRAND §3: Arabic body text gets 1.7× leading. */
private fun bodyLineHeight(fontSize: Int): androidx.compose.ui.unit.TextUnit = (fontSize * 1.7f).sp

val BrainingTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // The three that carry Arabic prose — 1.7 leading, per BRAND §3.
    bodyLarge = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = bodyLineHeight(16),
    ),
    bodyMedium = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = bodyLineHeight(14),
    ),
    bodySmall = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = bodyLineHeight(12),
    ),
    labelLarge = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BrainingFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)
