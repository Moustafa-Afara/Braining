package com.braining.core.ui.text

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Bidirectional-text support for an Arabic-first chat.
 *
 * ## The problem this solves
 *
 * Compose resolves paragraph direction **per paragraph** from the first strong
 * directional character it finds (`TextDirection.Content`, the default). In a chat whose
 * answers are Arabic prose sprinkled with English terms — `HTTP 401`, `BaseHttpProvider`,
 * `Ktor` — that produces a different direction for almost every line: a line that happens
 * to start with an English word is laid out left-to-right, the next line right-to-left,
 * and neutral characters (spaces, dots, commas, parentheses, digits) attach to whichever
 * side the algorithm resolved. The result is the interleaved, scrambled text the owner
 * reported. The model is not at fault and neither is the Unicode algorithm — it is being
 * handed the wrong paragraph direction.
 *
 * ## The fix
 *
 * Resolve ONE direction for the whole message from its dominant script, then force that
 * direction on every paragraph of the message. Once the paragraph direction is correct,
 * the Unicode bidirectional algorithm places embedded English runs correctly on its own.
 */
enum class BidiDirection {
    Rtl,
    Ltr,
    ;

    val layoutDirection: LayoutDirection
        get() = if (this == Rtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val textDirection: TextDirection
        get() = if (this == Rtl) TextDirection.Rtl else TextDirection.Ltr
}

/**
 * Share of strong characters that must be RTL for the whole block to be treated as RTL.
 *
 * Deliberately low, and deliberately NOT 50%. This is an Arabic-first product: an Arabic
 * sentence quoting a long English error message is still an Arabic sentence and must read
 * right-to-left. Only a block that is overwhelmingly Latin — a code listing, a pure
 * English answer — falls through to LTR.
 */
private const val RTL_SHARE_THRESHOLD_PERCENT = 30

/**
 * Below this many strong characters the sample is too small to judge, so the fallback
 * wins. This also stops the direction from flickering during streaming, when the first
 * token to arrive may be a single English word.
 */
private const val MIN_STRONG_CHARS = 4

/**
 * Resolves the paragraph direction of a whole block of text by counting strong
 * directional characters.
 *
 * Counting beats Android's usual "first strong character" heuristic here: `HTTP 401 يعني
 * أن المفتاح مرفوض` begins with a Latin run but is plainly an Arabic sentence, and
 * first-strong would lay it out left-to-right.
 *
 * `Character.getDirectionality` is used rather than hand-written Unicode ranges so that
 * Arabic Presentation Forms, Arabic Supplement, Hebrew and Latin-1 letters are all
 * classified correctly without maintaining a range table.
 */
fun CharSequence.resolveBidiDirection(
    fallback: BidiDirection = BidiDirection.Rtl,
): BidiDirection {
    var rtl = 0
    var ltr = 0
    var i = 0
    while (i < length) {
        val codePoint = Character.codePointAt(this, i)
        when (Character.getDirectionality(codePoint)) {
            Character.DIRECTIONALITY_RIGHT_TO_LEFT,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
            -> rtl++

            Character.DIRECTIONALITY_LEFT_TO_RIGHT -> ltr++
        }
        i += Character.charCount(codePoint)
    }

    val strong = rtl + ltr
    if (strong < MIN_STRONG_CHARS) return fallback
    return if (rtl * 100 >= strong * RTL_SHARE_THRESHOLD_PERCENT) {
        BidiDirection.Rtl
    } else {
        BidiDirection.Ltr
    }
}

/**
 * Text that lays itself out in the direction its own content dictates.
 *
 * Both the layout direction and the paragraph direction are provided, because they answer
 * different questions: [LayoutDirection] decides what `TextAlign.Start` and `padding.start`
 * mean, [TextDirection] decides how the bidirectional algorithm orders the runs. Setting
 * only one of them produces right-aligned text that still scrambles, or correctly ordered
 * text pinned to the wrong edge.
 *
 * Scoping the override to this composable — rather than forcing the whole screen — keeps
 * message placement, the app bar and the send button anchored to the app's own direction
 * while each individual message reads correctly.
 */
@Composable
fun BidiText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fallback: BidiDirection = BidiDirection.Rtl,
    /**
     * Overrides detection entirely. Use it for text whose direction is a property of the
     * format rather than of the language — a JSON body or a URL reads left-to-right even
     * when most of the characters inside it happen to be Arabic.
     */
    forced: BidiDirection? = null,
) {
    ProvideBidiDirection(text = text, fallback = fallback, forced = forced) { direction ->
        Text(
            text = text,
            modifier = modifier,
            color = color,
            style = style.copy(
                textDirection = direction.textDirection,
                textAlign = TextAlign.Start,
            ),
        )
    }
}

/**
 * Resolves [text]'s direction, installs it as the local [LayoutDirection], and hands it to
 * [content].
 *
 * Use this for composables that cannot take a [TextStyle] wholesale — a `TextField` needs
 * the direction applied to its own text style, its placeholder and its cursor at once.
 */
@Composable
fun ProvideBidiDirection(
    text: String,
    fallback: BidiDirection = BidiDirection.Rtl,
    forced: BidiDirection? = null,
    content: @Composable (BidiDirection) -> Unit,
) {
    val direction = remember(text, fallback, forced) {
        forced ?: text.resolveBidiDirection(fallback)
    }
    CompositionLocalProvider(LocalLayoutDirection provides direction.layoutDirection) {
        content(direction)
    }
}
