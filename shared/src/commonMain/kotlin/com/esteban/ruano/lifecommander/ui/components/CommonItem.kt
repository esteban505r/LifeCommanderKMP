package com.esteban.ruano.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CommonItem(
    modifier: Modifier = Modifier,
    title: String,
    isDone: Boolean,
    isEnabled: Boolean = true,
    isHovered: Boolean = false,
    interactionSource: MutableInteractionSource,
    borderColor: Color,
    leftIcon: @Composable (() -> Unit)? = null,
    topContent: @Composable (() -> Unit)? = null,
    rightContent: @Composable (() -> Unit)? = null,
    rightContentTextStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.body2,
    bottomContent: @Composable (() -> Unit)? = null,
    textDecoration: TextDecoration = TextDecoration.None,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    showContextMenu: Boolean,
    onDismissContextMenu: () -> Unit,
    contextMenuContent: @Composable ColumnScope.() -> Unit,
    itemWrapper: @Composable (content: @Composable () -> Unit) -> Unit = { content -> content() }
) {
    itemWrapper {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .then(
                    if (isDesktop()) Modifier
                        .hoverable(interactionSource)
                        .background(
                            if (isHovered) MaterialTheme.colors.primary.copy(alpha = 0.05f) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                    else Modifier
                )
                .then(
                    if (!isEnabled) Modifier.alpha(0.5f)
                    else Modifier
                )
                .clickable(enabled = isEnabled) { onClick() },
            elevation = when {
                !isEnabled -> 1.dp
                isHovered && isDesktop() -> 8.dp
                else -> 4.dp
            },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                when {
                    !isEnabled -> MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
                    isHovered && isDesktop() -> MaterialTheme.colors.primary.copy(alpha = 0.25f)
                    else -> borderColor.copy(alpha = 0.15f)
                }
            ),
            backgroundColor = when {
                !isEnabled -> MaterialTheme.colors.surface.copy(alpha = 0.8f)
                isHovered && isDesktop() -> MaterialTheme.colors.surface.copy(alpha = 0.98f)
                else -> MaterialTheme.colors.surface
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accent bar with improved styling
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(
                            if (isDone) MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                            else borderColor.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                )
                
                // Main content with consistent height
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Optional content above the title (e.g., tags)
                    if (topContent != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            topContent()
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left icon or checkbox with improved styling
                        Box(
                            modifier = Modifier.size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (leftIcon != null) {
                                leftIcon()
                            } else if (onCheckedChange != null) {
                                Checkbox(
                                    checked = isDone,
                                    onCheckedChange = onCheckedChange,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colors.primary,
                                        uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                                        checkmarkColor = MaterialTheme.colors.surface
                                    ),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Title with improved typography
                        Text(
                            text = title,
                            style = MaterialTheme.typography.body2.copy(
                                fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                                textDecoration = if (isDone) TextDecoration.LineThrough else textDecoration,
                            ),
                            overflow = TextOverflow.Ellipsis,
                            color = when {
                                !isEnabled -> MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                                isDone -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                else -> MaterialTheme.colors.onSurface.copy(alpha = 0.95f)
                            },
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Right content with improved spacing
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .defaultMinSize(minWidth = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CompositionLocalProvider(LocalTextStyle provides rightContentTextStyle) {
                                rightContent?.invoke()
                            }
                        }
                    }
                    
                    // Bottom content with improved spacing
                    if (bottomContent != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            bottomContent()
                        }
                    }
                }
            }
            
            if (!isDesktop()) {
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = onDismissContextMenu,
                    content = contextMenuContent
                )
            }
        }
    }
}

 