package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import models.TimerModel
import ui.theme.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun TimerCard(
    timer: TimerModel,
    isRunning: Boolean,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = Shapes.medium,
        elevation = 4.dp,
        backgroundColor = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timer.name,
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface
                    )
                    
                    Text(
                        text = "${timer.timeRemaining.toDuration(DurationUnit.MILLISECONDS).inWholeMinutes} minutes",
                        style = MaterialTheme.typography.body2,
                        color = TextSecondary
                    )
                }
                
                IconButton(
                    onClick = { showActions = !showActions },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colors.surface.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = if (showActions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showActions) "Hide actions" else "Show actions",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }
            
            AnimatedVisibility(
                visible = showActions,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(
                        color = DividerColor,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            onClick = onPlayClick,
                            icon = if (isRunning) {
                                @Composable {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Pause",
                                        tint = MaterialTheme.colors.primary
                                    )
                                }
                            } else {
                                @Composable {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = MaterialTheme.colors.primary
                                    )
                                }
                            },
                            contentDescription = if (isRunning) "Pause" else "Play",
                            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            iconColor = MaterialTheme.colors.primary
                        )
                        
                        ActionButton(
                            onClick = onStopClick,
                            icon = { Icons.Default.Delete },
                            contentDescription = "Stop",
                            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                            iconColor = MaterialTheme.colors.error
                        )
                        
                        ActionButton(
                            onClick = onEditClick,
                            icon = @androidx.compose.runtime.Composable {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colors.secondary
                                )
                            },
                            contentDescription = "Edit",
                            backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f),
                            iconColor = MaterialTheme.colors.secondary
                        )
                        
                        ActionButton(
                            onClick = onDeleteClick,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colors.error
                                )
                            },
                            contentDescription = "Delete",
                            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                            iconColor = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    contentDescription: String,
    backgroundColor: Color,
    iconColor: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
} 