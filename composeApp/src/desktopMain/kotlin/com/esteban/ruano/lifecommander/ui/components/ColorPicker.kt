package com.esteban.ruano.lifecommander.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.utils.UiUtils.parseHexColor

// Predefined color palette for tags
private val TAG_COLORS = listOf(
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
    "#82E0AA", // Light Green
    "#F4D03F", // Gold
    "#AF7AC5", // Lavender
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
            text = "Color",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        // Color Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (selectedColor != null) {
                            parseHexColor(selectedColor) ?: Color.Gray
                        } else {
                            Color.Gray
                        },
                        shape = CircleShape
                    ),
                color = if (selectedColor != null) {
                    parseHexColor(selectedColor) ?: Color.Transparent
                } else {
                    Color.Transparent
                }
            ) {
                if (selectedColor == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "None",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Text(
                text = selectedColor ?: "No color selected",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Color Options
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // None option
            item {
                ColorOption(
                    color = null,
                    isSelected = selectedColor == null,
                    onClick = { onColorSelected(null) }
                )
            }
            
            // Color options
            items(TAG_COLORS) { colorHex ->
                ColorOption(
                    color = colorHex,
                    isSelected = selectedColor == colorHex,
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colors.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                }
            ),
        color = if (color != null) {
            parseHexColor(color) ?: Color.Transparent
        } else {
            Color.Transparent
        }
    ) {
        if (color == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "None",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

