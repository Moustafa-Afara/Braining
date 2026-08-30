package com.braining.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * **مِداد**, mapped onto Material 3's roles.
 *
 * BRAND lists *tokens*; Material wants *roles*. The mapping that matters:
 *
 * - `primary` is the accent, and it is what a filled button is made of — so it is the violet, not
 *   the ground. In the old scheme `primary` was a pale indigo and the ground was the saturated
 *   one; that inversion is the redesign in one line.
 * - `secondaryContainer` is what `FilledTonalButton` fills itself with, so it carries `AccentSoft`.
 * - The `surfaceContainer*` ladder is set explicitly. Left unset, `darkColorScheme()` fills those
 *   slots from Material's own baseline purple, and cards — which read `surfaceContainerHighest` —
 *   would quietly render in a palette nobody chose. That is the same class of bug as the
 *   `dynamicColor` note below, and it is invisible until someone compares two screens.
 * - `tertiary` is amber. Material spends tertiary sparingly, which is exactly BRAND §2's rule.
 */
private val DarkColorScheme = darkColorScheme(
    primary = BrandPalette.Ink.Accent,
    onPrimary = BrandPalette.Ink.OnAccent,
    primaryContainer = BrandPalette.Ink.AccentSoft,
    onPrimaryContainer = BrandPalette.Ink.Accent,

    secondary = BrandPalette.Ink.Accent,
    onSecondary = BrandPalette.Ink.OnAccent,
    secondaryContainer = BrandPalette.Ink.AccentSoft,
    onSecondaryContainer = BrandPalette.Ink.Accent,

    tertiary = BrandPalette.Ink.Amber,
    onTertiary = BrandPalette.OnAmber,
    tertiaryContainer = BrandPalette.Ink.AmberSoft,
    onTertiaryContainer = BrandPalette.Ink.Amber,

    background = BrandPalette.Ink.Ground,
    onBackground = BrandPalette.Ink.Text,
    surface = BrandPalette.Ink.Ground,
    onSurface = BrandPalette.Ink.Text,
    surfaceVariant = BrandPalette.Ink.Surface,
    onSurfaceVariant = BrandPalette.Ink.Muted,

    surfaceContainerLowest = BrandPalette.Ink.Ground,
    surfaceContainerLow = BrandPalette.Ink.Surface,
    surfaceContainer = BrandPalette.Ink.Surface,
    surfaceContainerHigh = BrandPalette.Ink.Raised,
    surfaceContainerHighest = BrandPalette.Ink.Surface,
    surfaceDim = BrandPalette.Ink.Ground,
    surfaceBright = BrandPalette.Ink.Raised,

    inverseSurface = BrandPalette.Ink.Text,
    inverseOnSurface = BrandPalette.Ink.Ground,
    inversePrimary = BrandPalette.Paper.Accent,

    outline = BrandPalette.Ink.Line,
    outlineVariant = BrandPalette.Ink.Line,
    scrim = BrandPalette.Ink.Ground,

    error = BrandPalette.ErrorLight,
    onError = BrandPalette.ErrorDark,
    errorContainer = BrandPalette.ErrorDark,
    onErrorContainer = BrandPalette.ErrorLight,
)

/** The same roles on paper. The accent darkens, because it now has to carry contrast on white. */
private val LightColorScheme = lightColorScheme(
    primary = BrandPalette.Paper.Accent,
    onPrimary = BrandPalette.Paper.OnAccent,
    primaryContainer = BrandPalette.Paper.AccentSoft,
    onPrimaryContainer = BrandPalette.Paper.Accent,

    secondary = BrandPalette.Paper.Accent,
    onSecondary = BrandPalette.Paper.OnAccent,
    secondaryContainer = BrandPalette.Paper.AccentSoft,
    onSecondaryContainer = BrandPalette.Paper.Accent,

    tertiary = BrandPalette.Paper.Amber,
    onTertiary = BrandPalette.Paper.OnAccent,
    tertiaryContainer = BrandPalette.Paper.AmberSoft,
    onTertiaryContainer = BrandPalette.Paper.Amber,

    background = BrandPalette.Paper.Ground,
    onBackground = BrandPalette.Paper.Text,
    surface = BrandPalette.Paper.Ground,
    onSurface = BrandPalette.Paper.Text,
    surfaceVariant = BrandPalette.Paper.Raised,
    onSurfaceVariant = BrandPalette.Paper.Muted,

    surfaceContainerLowest = BrandPalette.Paper.Surface,
    surfaceContainerLow = BrandPalette.Paper.Surface,
    surfaceContainer = BrandPalette.Paper.Surface,
    surfaceContainerHigh = BrandPalette.Paper.Raised,
    surfaceContainerHighest = BrandPalette.Paper.Surface,
    surfaceDim = BrandPalette.Paper.Raised,
    surfaceBright = BrandPalette.Paper.Surface,

    inverseSurface = BrandPalette.Paper.Text,
    inverseOnSurface = BrandPalette.Paper.Ground,
    inversePrimary = BrandPalette.Ink.Accent,

    outline = BrandPalette.Paper.Line,
    outlineVariant = BrandPalette.Paper.Line,
    scrim = BrandPalette.Paper.Text,

    error = BrandPalette.Error,
    onError = BrandPalette.Paper.OnAccent,
    errorContainer = BrandPalette.ErrorPale,
    onErrorContainer = BrandPalette.ErrorDeep,
)

/**
 * ## `dynamicColor` is gone, and that is still the single most important line of this file
 *
 * This function used to take `dynamicColor: Boolean = true` and call
 * `dynamicDarkColorScheme(context)` on Android 12+. The test device is Android 14 — so **every
 * screen rendered in colours sampled from the user's wallpaper**, and no palette defined here
 * could reach the display. The parameter is deleted rather than defaulted to `false`: a switch
 * that must never be flipped is worse than no switch, and re-adding it silently deletes the brand.
 *
 * @param darkTheme follows the system. BRAND §2 calls dark "the default" and light "fully
 *   supported"; honouring the OS setting is how a user who has chosen light gets light.
 */
@Composable
fun BrainingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // `background`, not `primary`: the status bar continues the surface behind it.
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BrainingTypography,
        shapes = BrainingShapes,
        content = content,
    )
}
