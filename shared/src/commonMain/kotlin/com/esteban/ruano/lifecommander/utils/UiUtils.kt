package com.esteban.ruano.lifecommander.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.esteban.ruano.ui.DarkGray

object UiUtils{

    /**
     * Parse a hex color string (e.g., "#FFAA00" or "#FFAA00FF") to a Color object.
     * Supports formats: #RRGGBB, #AARRGGBB
     */
    fun parseHexColor(hex: String): Color? {
        return try {
            val cleanHex = hex.trim().removePrefix("#")
            when (cleanHex.length) {
                6 -> {
                    // #RRGGBB format
                    val r = cleanHex.substring(0, 2).toInt(16)
                    val g = cleanHex.substring(2, 4).toInt(16)
                    val b = cleanHex.substring(4, 6).toInt(16)
                    Color(r, g, b)
                }
                8 -> {
                    // #AARRGGBB format
                    val a = cleanHex.substring(0, 2).toInt(16)
                    val r = cleanHex.substring(2, 4).toInt(16)
                    val g = cleanHex.substring(4, 6).toInt(16)
                    val b = cleanHex.substring(6, 8).toInt(16)
                    Color(r / 255f, g / 255f, b / 255f, a / 255f)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get color for priority level
     * URGENT (4): Red - urgent/critical
     * HIGH (3): Orange - important
     * MEDIUM (2): Yellow/Amber - normal
     * LOW (1): Green - low priority
     * NONE (0): Gray - no priority
     */
    fun getColorByPriority(priority: Int): Color {
        return when (priority) {
            4 -> Color(0xFFD32F2F) // Red - Urgent
            3 -> Color(0xFFFF6F00) // Orange - High
            2 -> Color(0xFFFFA000) // Amber - Medium
            1 -> Color(0xFF388E3C) // Green - Low
            else -> Color(0xFF757575) // Gray - None
        }
    }

    /**
     * Get icon for priority level
     * URGENT: Error icon (critical)
     * HIGH: Warning icon (important)
     * MEDIUM: Info icon (normal)
     * LOW: CheckCircle icon (low priority)
     * NONE: Remove icon (no priority)
     */
    fun getIconByPriority(priority: Int): ImageVector {
        return when (priority) {
            4 -> Icons.Filled.Error // Urgent - critical error icon
            3 -> Icons.Filled.Warning // High - warning icon
            2 -> Icons.Filled.Info // Medium - info icon
            1 -> Icons.Filled.CheckCircle // Low - check circle
            else -> Icons.Filled.Remove // None - remove/minus icon
        }
    }

}