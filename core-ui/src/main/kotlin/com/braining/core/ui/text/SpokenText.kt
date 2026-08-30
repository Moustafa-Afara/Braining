package com.braining.core.ui.text

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle

/**
 * The answer, with the word being spoken aloud marked — and the page following it, until the
 * reader takes over.
 *
 * The owner's request, 2026-08-28: pressing «استمع» should take him to the word the model is
 * saying and carry the page down as it goes, **without ever fighting him for the scroll**. That
 * second half is the whole design problem. An auto-scroll that reasserts itself is worse than no
 * auto-scroll: the user drags away to re-read a line and is yanked back mid-sentence, and the
 * only way to win is to stop the audio.
 *
 * So the rule is one-directional: **the app follows until the user touches the page, and then it
 * stops until the user says otherwise.** [follow] is that switch, owned by the caller because the
 * touch that cancels it lands on the scroll container, not on this text.
 *
 * ### Direction is still decided in one place
 *
 * This renders through the same resolution `BidiText` uses rather than reimplementing it — the
 * single point of change hard constraint 6 established, and the one that cost a day in M1.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpokenText(
    text: String,
    /** The character range being spoken, or null. Out-of-bounds values are ignored, not clamped. */
    spoken: IntRange?,
    /** False the moment the user scrolls: see the KDoc. */
    follow: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fallback: BidiDirection = BidiDirection.Rtl,
) {
    // A gentle pulse rather than a static block. The mark has to be findable at a glance on a
    // page of dense Arabic, and motion is what the eye catches — but it is slow (900 ms) and
    // narrow in range, because a highlight that flashes is a highlight you turn the feature off
    // to escape.
    val pulse = rememberInfiniteTransition(label = "spoken")
    val alpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "spokenAlpha",
    )

    val highlight = MaterialTheme.colorScheme.primary

    val requester = remember { BringIntoViewRequester() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    // **Minimal scrolling, and only when following.** `bringIntoView` walks up to whatever
    // scrollable actually contains this text and moves it the smallest distance that reveals the
    // rect — so a word already on screen produces no movement at all. Computing an absolute
    // offset by hand would have to know how deeply this composable is nested, which is exactly
    // the kind of assumption that breaks the day a layout changes.
    LaunchedEffect(spoken, follow, layout) {
        if (!follow) return@LaunchedEffect
        val result = layout ?: return@LaunchedEffect
        val range = spoken ?: return@LaunchedEffect
        if (range.first < 0 || range.first >= text.length) return@LaunchedEffect
        val box: Rect = runCatching { result.getBoundingBox(range.first) }.getOrNull()
            ?: return@LaunchedEffect
        // Inflated vertically so the spoken line lands inside the page rather than flush against
        // its edge, where the next word would immediately scroll again.
        runCatching {
            requester.bringIntoView(box.inflate(box.height))
        }
    }

    // The same resolution `BidiText` performs, through the same function — direction is decided
    // in one place in this app and this composable is not allowed to be a second one.
    ProvideBidiDirection(text = text, fallback = fallback) { direction ->
        Text(
            // **A plain String, and the mark is drawn rather than styled into it.**
            //
            // The first version built an `AnnotatedString` whose span carried the pulsing colour.
            // That colour changes sixty times a second, so the whole annotated string was rebuilt
            // and the whole answer re-laid-out on every frame — and because each relayout emitted
            // a new `TextLayoutResult`, the `LaunchedEffect` below restarted continuously and its
            // scroll animation never got past its first frame. The follow-along starved itself.
            //
            // Drawing the highlight instead confines the pulse to the **draw phase**: the text is
            // measured once per answer, the path is recomputed only when the spoken word moves,
            // and the colour animates for free. `getPathForRange` also spans line wraps, which a
            // rectangle behind a `SpanStyle` never did.
            text = text,
            modifier = modifier
                .bringIntoViewRequester(requester)
                .drawBehind {
                    val result = layout ?: return@drawBehind
                    val range = spoken ?: return@drawBehind
                    if (range.first < 0 || range.last >= text.length) return@drawBehind
                    if (range.first > range.last) return@drawBehind
                    // Behind the glyphs, so the word stays fully legible on top of it — a mark
                    // that has to be read *through* is a mark that costs the reader the word.
                    val path = runCatching {
                        result.getPathForRange(range.first, range.last + 1)
                    }.getOrNull() ?: return@drawBehind
                    drawPath(path, highlight.copy(alpha = alpha))
                },
            style = style.copy(textDirection = direction.textDirection),
            color = color,
            onTextLayout = { layout = it },
        )
    }
}
