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
                .padding(
                    horizontal = 16.dp,
                    vertical =  8.dp
                )
                .then(
                    if (isDesktop()) Modifier
                        .hoverable(interactionSource)
                        .background(
                            if (isHovered) Color.LightGray.copy(alpha = 0.05f) else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                    else Modifier
                )
                .then(
                    if (!isEnabled) Modifier.alpha(0.5f)
                    else Modifier
                )
                .clickable(enabled = isEnabled) { onClick() },
            elevation = if (!isEnabled) 2.dp else 4.dp,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                1.dp,
                if (!isEnabled) MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                else if (isHovered && isDesktop()) Color.LightGray.copy(alpha = 0.3f)
                else borderColor.copy(alpha = 0.6f)
            ),
            backgroundColor = if (!isEnabled) MaterialTheme.colors.surface.copy(alpha = 0.8f)
            else MaterialTheme.colors.surface
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .height(120.dp)
                        .requiredHeight(120.dp)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left: Icon/Checkbox and title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (leftIcon != null) {
                                leftIcon()
                            } else if (onCheckedChange != null) {
                                Checkbox(
                                    checked = isDone,
                                    onCheckedChange = onCheckedChange,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colors.primary,
                                        uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.body1.copy(
                                    textDecoration = if (isDone) TextDecoration.LineThrough else textDecoration
                                ),
                                color = when {
                                    !isEnabled -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    isDone -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colors.onSurface
                                }
                            )
                        }

                        // Right content
                        rightContent?.invoke()
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom content
                    bottomContent?.invoke()
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
}

 