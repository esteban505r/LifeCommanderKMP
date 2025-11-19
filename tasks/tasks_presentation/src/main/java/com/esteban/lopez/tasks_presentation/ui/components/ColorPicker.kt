package com.esteban.ruano.tasks_presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.utils.UiUtils.parseHexColor
import com.esteban.ruano.ui.Gray2

/**
 * Predefined color palette for tags
 */
val TAG_COLORS = listOf(
    "#FF6B6B", // Red
    "#4ECDC4", // Teal
    "#45B7D1", // Blue
    "#FFA07A", // Light Salmon
    "#98D8C8", // Mint
    "#F7DC6F", // Yellow
    "#BB8FCE", // Purple
    "#85C1E2", // Sky Blue
    "#F8B739", // Orange
    "#52BE80", // Green
    "#EC7063", // Coral
    "#5DADE2", // Light Blue
    "#F1948A", // Pink
    "#58D68D", // Light Green
    "#F4D03F", // Gold
    "#AF7AC5", // Lavender
    "#5DADE2", // Cyan
    "#F39C12", // Dark Orange
    "#E74C3C", // Dark Red
    "#3498DB", // Bright Blue
    "#1ABC9C", // Turquoise
    "#9B59B6", // Amethyst
    "#E67E22", // Carrot
    "#2ECC71", // Emerald
    "#34495E", // Dark Gray
    "#95A5A6", // Gray
    "#7F8C8D", // Darker Gray
    "#16A085", // Dark Teal
    "#27AE60", // Dark Green
    "#2980B9", // Dark Blue
)

@Composable
fun ColorPicker(
    selectedColor: String?,
    onColorSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Select Color",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        // Color grid
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // None option
            item {
                ColorOption(
                    color = null,
                    isSelected = selectedColor == null,
                    onClick = { onColorSelected(null) },
                    label = "None"
                )
            }
            
            // Color options
            items(TAG_COLORS) { colorHex ->
                val color = parseHexColor(colorHex) ?: Color.Transparent
                ColorOption(
                    color = color,
                    isSelected = selectedColor == colorHex,
                    onClick = { onColorSelected(colorHex) },
                    label = null
                )
            }
        }
        
        // Selected color preview
        if (selectedColor != null) {
            val previewColor = parseHexColor(selectedColor) ?: Color.Transparent
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(previewColor, CircleShape)
                        .border(1.dp, Gray2.copy(alpha = 0.3f), CircleShape)
                )
                Text(
                    text = "Selected: $selectedColor",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
    label: String?
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (color == null) {
            // None option - show a border with X or text
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(
                    width = if (isSelected) 3.dp else 2.dp,
                    color = if (isSelected) MaterialTheme.colors.primary else Gray2.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label ?: "×",
                        style = MaterialTheme.typography.body2,
                        color = if (isSelected) MaterialTheme.colors.primary else Gray2
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color,
                border = BorderStroke(
                    width = if (isSelected) 3.dp else 2.dp,
                    color = if (isSelected) MaterialTheme.colors.primary else Color.White
                ),
                elevation = if (isSelected) 4.dp else 0.dp
            ) {}
        }
    }
}

