package com.example.todoapp.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════════════════════════
//  Shape system — Extra-large corner radii everywhere for the cute Baemin vibe
// ══════════════════════════════════════════════════════════════════════════════

val TodoShapes = Shapes(
    // Used by: small chips, snackbars, tooltips
    extraSmall = RoundedCornerShape(8.dp),

    // Used by: TextFields, small cards, menu items
    small = RoundedCornerShape(12.dp),

    // Used by: Buttons, standard cards
    medium = RoundedCornerShape(20.dp),

    // Used by: Dialogs, bottom sheets, task cards
    large = RoundedCornerShape(28.dp),

    // Used by: Full-screen sheets, hero cards, FAB container
    extraLarge = RoundedCornerShape(36.dp),
)

// ── Convenience shape constants referenced directly in composables ─────────────

/** Pill / stadium shape — used for primary buttons */
val PillShape = RoundedCornerShape(50)

/** Rounded card shape — used for TaskCard */
val CardShape = RoundedCornerShape(24.dp)

/** TextField shape — rounded but not pill */
val TextFieldShape = RoundedCornerShape(16.dp)

/** Bottom-sheet top corners only */
val BottomSheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

/** Top bar / hero banner — bottom corners only */
val HeroBannerShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)

/** Small chip / badge */
val ChipShape = CircleShape

/** Dialog  */
val DialogShape = RoundedCornerShape(32.dp)
