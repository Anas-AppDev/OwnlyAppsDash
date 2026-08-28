package com.ownly.dash.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Shared corner radii for inputs, tabs, cards, and buttons. */
object DashShapes {
    val field = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(12.dp)
    val button = RoundedCornerShape(12.dp)
    /** Fully rounded pill shape for tab bar and badges. */
    val pill = RoundedCornerShape(50)
}
