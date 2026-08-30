package com.braining.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.braining.core.ui.theme.BrainingShape

/**
 * The app's buttons — **four weights, one shape, and a finger that gets an answer.**
 *
 * The owner's complaint on 2026-08-18 was «ليس فيه تصاميم أزرار تفاعلية وعصرية». Two things were
 * missing and both are here:
 *
 * 1. **A shape that is not a pill.** Material's default button is fully rounded, which reads as a
 *    tag or a chip rather than a control. [BrainingShape.Button] is 16dp — soft enough to be
 *    modern, square enough to still be a button.
 * 2. **A physical response.** A press shrinks the button to 96% on a spring and releases. This is
 *    the whole difference between a control that feels alive and one that feels like a picture of
 *    a control; Material's ripple alone is a colour change, and on a dark ground it is nearly
 *    invisible.
 *
 * **A wrapper narrows an API, and the narrowing has to be deliberate.** The first version of this
 * file omitted `contentPadding`, and the option buttons in Clarify — which set their own, because
 * an option is a sentence and not a word — stopped compiling. Every parameter a call site already
 * uses has to survive the wrapping; anything else is a rewrite disguised as a refactor.
 *
 * **Why wrappers rather than styling every call site.** A shape passed by hand at twenty call
 * sites is a shape that will be right at nineteen of them. This is the same reasoning that put
 * `ProviderId.defaultModel` in one place after a model name was wrong in three files at once.
 *
 * **Choosing a weight** — the hierarchy is the point, not decoration:
 * - [PrimaryButton] — the one action the screen exists for. **One per screen.**
 * - [TonalButton] — a real action that repeats, and must not shout: regenerate, swap.
 * - [QuietButton] — an alternative or a way out: cancel, an option among several.
 * - [InsightButton] — amber, and reserved. «نضجت الفكرة» and nothing else, by BRAND §2's rule
 *   that amber marks the moment of understanding and never a routine action.
 */

/**
 * Shrink to 96% while held.
 *
 * A spring rather than a tween, deliberately: a tween of a fixed duration feels mechanical at the
 * release, where a spring settles the way a physical key does. Damping is high enough that it does
 * not visibly bounce — this is weight, not playfulness.
 */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 1200f),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** The one action the screen exists for. Filled, in the accent. */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = BrainingShape.Button,
        interactionSource = interaction,
        contentPadding = contentPadding,
        content = content,
    )
}

/** A real action that repeats and must not shout. Filled with the soft accent. */
@Composable
fun TonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = BrainingShape.Button,
        interactionSource = interaction,
        contentPadding = contentPadding,
        content = content,
    )
}

/** An alternative, or a way out. Outlined, no fill. */
@Composable
fun QuietButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = BrainingShape.Button,
        interactionSource = interaction,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Amber, and **reserved for the moment of understanding** — «نضجت الفكرة».
 *
 * BRAND §2: "if more than one amber element competes on a screen, the accent has been overused —
 * remove one." A second use of this button anywhere is that overuse.
 */
@Composable
fun InsightButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = BrainingShape.Button,
        interactionSource = interaction,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        ),
        contentPadding = contentPadding,
        content = content,
    )
}
