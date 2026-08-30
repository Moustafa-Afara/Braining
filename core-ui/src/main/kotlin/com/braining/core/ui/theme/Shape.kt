package com.braining.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii — **raised across the board on 2026-08-18**, overriding `docs/BRAND.md` §5's
 * "8dp controls, 12dp cards".
 *
 * A tight radius is the single detail that dates an interface fastest. It is a leftover from
 * interfaces that imitated physical buttons; a modern control is treated as a *touch area*, and
 * touch areas are drawn soft. The owner's words on the old screens were «ليس فيه تصاميم أزرار
 * تفاعلية وعصرية», and this is half of that answer — the other half is
 * `components/BrainingButtons.kt`, which makes them respond to the finger.
 *
 * Material 3 reads these five slots for every component it draws, so setting them here changes
 * text fields, cards, menus and dialogs at once. **Buttons are the exception**: Material gives
 * them a fully-rounded pill by default, so the button wrappers set [Button] explicitly.
 */
val BrainingShapes = Shapes(
    /** Text fields. */
    extraSmall = RoundedCornerShape(10.dp),
    /** Chips and small containers. */
    small = RoundedCornerShape(14.dp),
    /** Cards — the turn cards, the provider cards, the answer. */
    medium = RoundedCornerShape(20.dp),
    /** Large containers. */
    large = RoundedCornerShape(24.dp),
    /** The recording panel and dialogs. */
    extraLarge = RoundedCornerShape(28.dp),
)

/** Radii Material does not read from [BrainingShapes]. */
object BrainingShape {
    /** Every button in the app. Not a pill: a pill reads as a tag, a 16dp rect reads as a button. */
    val Button = RoundedCornerShape(16.dp)
}
