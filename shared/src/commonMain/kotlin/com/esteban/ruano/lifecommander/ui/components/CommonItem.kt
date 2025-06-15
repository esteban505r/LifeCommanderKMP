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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .then(
                    if (isDesktop()) Modifier
                        .hoverable(interactionSource)
                        .background(
                            if (isHovered) MaterialTheme.colors.primary.copy(alpha = 0.07f) else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                    else Modifier
                )
                .then(
                    if (!isEnabled) Modifier.alpha(0.5f)
                    else Modifier
                )
                .clickable(enabled = isEnabled) { onClick() },
            elevation = if (!isEnabled) 1.dp else 4.dp,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                1.dp,
                when {
                    !isEnabled -> MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
                    isHovered && isDesktop() -> MaterialTheme.colors.primary.copy(alpha = 0.18f)
                    else -> borderColor.copy(alpha = 0.18f)
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
                // Accent bar
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(
                            if (isDone) MaterialTheme.colors.onSurface.copy(alpha = 0.15f)
                            else MaterialTheme.colors.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                        )
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Main content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp),
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
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.subtitle1.copy(
                                fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                                textDecoration = if (isDone) TextDecoration.LineThrough else textDecoration
                            ),
                            color = when {
                                !isEnabled -> MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                                isDone -> MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                else -> MaterialTheme.colors.onSurface.copy(alpha = 0.92f)
                            },
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        // Right content
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .defaultMinSize(minWidth = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CompositionLocalProvider(LocalTextStyle provides rightContentTextStyle) {
                                rightContent?.invoke()
                            }
                        }
                    }
                    // Bottom content
                    if (bottomContent != null) {
                        Spacer(modifier = Modifier.height(8.dp))
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

 