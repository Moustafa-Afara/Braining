package com.braining.core.ui.voice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

/**
 * The live recording visualiser — **and the logo**.
 *
 * `docs/BRAND.md` §6: "the mark's waveform is the live audio visualiser — the bars react to
 * real input amplitude. This is the signature interaction of the app; get it right." So the
 * geometry and colours below are the mark's, not a decoration that happens to resemble it:
 * five bars rising to a tall centre, a dot floating above, the accent outside and inside, and
 * amber for the centre bar and the dot.
 *
 * **The two colours follow the theme** — `primary` and `tertiary` — which is a change made with
 * the مِداد redesign on 2026-08-18. BRAND §1's "a logo does not re-tint itself" held while there
 * was one saturated ground; with a near-black dark theme and a white light theme, a single fixed
 * violet would be glaring on one and washed out on the other. The *form* is fixed, which is what
 * the rule is protecting; the two hues follow the surface they are drawn on. The proportions are read off
 * `assets/logo/icon.svg` — outer 82 / inner 143 / centre 215 units tall, at 36 wide with fully
 * rounded ends, spaced 67 apart.
 *
 * **It must move with real sound.** A decorative animation that ignores the microphone is the
 * single outcome `TranscriptionEvent.Amplitude` exists to prevent. Silence settles the bars to
 * their resting logo shape; speech drives them up. That resting state is deliberate — at
 * amplitude 0 this composable *is* the logo, so the user sees the app's mark come alive rather
 * than a generic meter appear.
 *
 * @param amplitude 0f..1f, straight from `TranscriptionEvent.Amplitude`.
 */
@Composable
fun BrainingWaveform(
    amplitude: Float,
    modifier: Modifier = Modifier,
) {
    // Smoothed, or the bars flicker: onRmsChanged fires far faster than the eye can follow,
    // and BRAND §5 caps state-change motion at 150ms.
    val level by animateFloatAsState(
        targetValue = amplitude.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 120),
        label = "waveformAmplitude",
    )

    // Read outside the draw scope: `MaterialTheme` is a composition local and the Canvas lambda
    // runs at draw time, not composition time.
    val accent = MaterialTheme.colorScheme.primary
    val insight = MaterialTheme.colorScheme.tertiary
    // The outer pair sits back a step. Alpha on the accent rather than a second token: it is the
    // same tonal-step reasoning the error family is built on, and it keeps the mark to two hues.
    val barColours = listOf(accent.copy(alpha = 0.62f), accent, insight, accent, accent.copy(alpha = 0.62f))

    Canvas(modifier = modifier.size(width = 132.dp, height = 96.dp)) {
        val unit = size.width / TOTAL_UNITS_WIDE
        val barWidth = BAR_WIDTH_UNITS * unit
        val radius = CornerRadius(barWidth / 2f, barWidth / 2f)
        val centreY = size.height * 0.62f

        BAR_RESTING_HEIGHTS.forEachIndexed { index, restingUnits ->
            // Each bar grows toward the centre bar's height, the outer ones travelling
            // furthest — so loud speech converges on the mark's own silhouette instead of
            // flattening it. GAIN keeps the tallest bar inside the canvas at amplitude 1.
            val reach = (CENTRE_HEIGHT_UNITS - restingUnits) * GAIN
            val heightUnits = restingUnits + reach * level
            val barHeight = heightUnits * unit

            val left = (SIDE_MARGIN_UNITS + index * BAR_PITCH_UNITS) * unit
            drawRoundRect(
                color = barColours[index],
                topLeft = Offset(left, centreY - barHeight / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = radius,
            )
        }

        // The nuqta of ف in «فهم», and the moment of insight (BRAND §1). It rides just above
        // the centre bar, so it lifts as the user speaks — the one element that reads as
        // "understanding is arriving".
        val centreResting = BAR_RESTING_HEIGHTS[2]
        val centreHeight = (centreResting + (CENTRE_HEIGHT_UNITS - centreResting) * GAIN * level) * unit
        val dotRadius = DOT_RADIUS_UNITS * unit
        drawCircle(
            color = insight,
            radius = dotRadius,
            center = Offset(
                x = size.width / 2f,
                y = centreY - centreHeight / 2f - dotRadius * DOT_GAP_FACTOR,
            ),
        )
    }
}

// Proportions from assets/logo/icon.svg, kept in the SVG's own units so the mark can be
// checked against the file rather than against someone's memory of it.
private const val BAR_WIDTH_UNITS = 36f
private const val BAR_PITCH_UNITS = 67f
private const val SIDE_MARGIN_UNITS = 8f
private const val TOTAL_UNITS_WIDE = SIDE_MARGIN_UNITS * 2 + BAR_WIDTH_UNITS + BAR_PITCH_UNITS * 4
private const val CENTRE_HEIGHT_UNITS = 215f
private const val DOT_RADIUS_UNITS = 23f
private const val DOT_GAP_FACTOR = 1.9f

/** How far a bar travels toward the centre bar's height at full amplitude. */
private const val GAIN = 0.85f

private val BAR_RESTING_HEIGHTS = floatArrayOf(82f, 143f, 215f, 143f, 82f)
