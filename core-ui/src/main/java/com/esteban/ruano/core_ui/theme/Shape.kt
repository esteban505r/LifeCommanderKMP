package com.esteban.ruano.core_ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

// Modern Home Screen Shapes (Flattened Structure)
object HomeShapes {
    val WelcomeCard = RoundedCornerShape(24.dp)
    val SectionCard = RoundedCornerShape(20.dp)
    val TaskCard = RoundedCornerShape(16.dp)
    val HabitCard = RoundedCornerShape(20.dp)
    val Button = RoundedCornerShape(24.dp)
    val IconButton = RoundedCornerShape(26.dp)
    val Header = RoundedCornerShape(12.dp)
    val PriorityTag = RoundedCornerShape(8.dp)
    val VersionBadge = RoundedCornerShape(12.dp)
}