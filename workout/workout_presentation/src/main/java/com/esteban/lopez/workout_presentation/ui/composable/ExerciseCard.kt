package com.esteban.ruano.workout_presentation.ui.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.workout_domain.model.Exercise

private val CardBackground = Color(0xFFF7F7FA) // Custom light gray for card
private val CardBorder = Color(0xFFE0E0E0)
private val ChipBackground = Color(0xFFF0F0F5)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    onUpdate: (Exercise) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onCompleteExercise: (() -> Unit)? = null,
    isCompleted: Boolean = false,
    showActionButtons: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300)),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = CardBackground,
        elevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) MaterialTheme.colors.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colors.primary.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isCompleted) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter,
                            contentDescription = "Exercise",
                            tint = if (isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                exercise.name,
                                style = MaterialTheme.typography.subtitle1.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                                )
                            )
                            if (isCompleted) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier.size(15.dp).padding(start = 4.dp)
                                )
                            }
                        }
                        if (exercise.description.isNotBlank()) {
                            Text(
                                exercise.description,
                                style = MaterialTheme.typography.body2.copy(
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                if (showActionButtons) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBorder.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        if (onCompleteExercise != null) {
                            IconButton(
                                onClick = onCompleteExercise,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = if (isCompleted) "Completed" else "Complete Exercise",
                                    tint = if (isCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { onUpdate(exercise) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colors.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDelete(exercise.id ?: "") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colors.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            if (exercise.baseSets > 0 || exercise.baseReps > 0 || exercise.restSecs > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(
                    color = CardBorder.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (exercise.baseSets > 0) {
                        StatChip(
                            text = "${exercise.baseSets}",
                            label = "sets",
                            backgroundColor = ChipBackground,
                            textColor = MaterialTheme.colors.primary
                        )
                    }
                    if (exercise.baseReps > 0) {
                        StatChip(
                            text = "${exercise.baseReps}",
                            label = "reps",
                            backgroundColor = ChipBackground,
                            textColor = MaterialTheme.colors.secondary
                        )
                    }
                    if (exercise.restSecs > 0) {
                        StatChip(
                            text = "${exercise.restSecs}",
                            label = "rest",
                            backgroundColor = ChipBackground,
                            textColor = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    text: String,
    label: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.caption.copy(
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
