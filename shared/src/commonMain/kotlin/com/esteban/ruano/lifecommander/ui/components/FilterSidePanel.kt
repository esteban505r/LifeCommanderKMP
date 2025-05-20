package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun FilterSidePanel(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit,
    hasActiveFilters: Boolean,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.32f))
                    .zIndex(1f)
                    .clickable(onClick = onDismiss)
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(initialOffsetX = { screenWidth }),
            exit = slideOutHorizontally(targetOffsetX = { screenWidth })
        ) {
            Surface(
                Modifier
                    .fillMaxHeight()
                    .width(400.dp)
                    .align(Alignment.CenterEnd)
                    .zIndex(2f),
                elevation = 8.dp
            ) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filters", style = MaterialTheme.typography.h6)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (hasActiveFilters) {
                                TextButton(onClick = onClearFilters) { Text("Clear All") }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close Filters")
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        content()
                    }
                }
            }
        }
    }
}
