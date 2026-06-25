package com.example.todoapp.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════════════════════════
//  Shape system — Modern Buddy / Stitch design tokens
//  DEFAULT: 1rem | lg: 2rem | xl: 3rem | full: pill
// ══════════════════════════════════════════════════════════════════════════════

val TodoShapes = Shapes(
    // Chips, snackbars
    extraSmall = RoundedCornerShape(8.dp),

    // Inputs, small cards
    small = RoundedCornerShape(16.dp),   // ~1rem

    // Buttons, standard cards
    medium = RoundedCornerShape(16.dp),

    // Dialogs, task cards, glass cards
    large = RoundedCornerShape(24.dp),   // Stitch "rounded-xl" ≈ 2rem

    // Hero panels, bottom sheets
    extraLarge = RoundedCornerShape(36.dp),
)

// ── Named shape constants ─────────────────────────────────────────────────────

/** Pill / stadium — primary buttons, bottom nav items, input fields */
val PillShape = RoundedCornerShape(50)

/** Glass card — task cards, greeting panel */
val CardShape = RoundedCornerShape(24.dp)      // rounded-xl in Stitch

/** Input field — pill for single-line, rounded for textarea */
val TextFieldShape = PillShape

/** Textarea / multi-line field */
val TextAreaShape = RoundedCornerShape(24.dp)  // textarea-pill in Stitch

/** Bottom-sheet — top corners only */
val BottomSheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

/** Hero banner — bottom corners only */
val HeroBannerShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

/** Floating bottom nav bar */
val FloatingNavShape = RoundedCornerShape(9999.dp)

/** Small chip / badge */
val ChipShape = CircleShape

/** Dialog */
val DialogShape = RoundedCornerShape(32.dp)
