package com.esteban.ruano.core_ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {
    fun getIconByString(icon: String): ImageVector {
        return when (icon) {
            "home" -> Icons.Default.Home
            "habits" -> Icons.Default.Done
            "tasks" -> Icons.Default.DateRange
            "workout" -> Icons.Default.Person
            "nutrition" -> Icons.Default.Favorite
            else -> Icons.Default.Home
        }
    }
    fun getResourceIconByString(icon: String): Int {
        return when (icon) {
            "home" -> com.esteban.ruano.core_ui.R.drawable.ic_home
            "habits" -> com.esteban.ruano.core_ui.R.drawable.ic_habits
            "tasks" -> com.esteban.ruano.core_ui.R.drawable.ic_tasks
            "workout" -> com.esteban.ruano.core_ui.R.drawable.ic_workout
            "nutrition" -> com.esteban.ruano.core_ui.R.drawable.ic_nutrition
            else -> com.esteban.ruano.core_ui.R.drawable.ic_home
        }
    }
}