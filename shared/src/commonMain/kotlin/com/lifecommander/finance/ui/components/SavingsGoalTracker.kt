package com.lifecommander.finance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.utils.DateUIUtils.formatCurrency
import com.lifecommander.finance.model.SavingsGoal
import com.lifecommander.finance.model.SavingsGoalProgress

@Composable
fun SavingsGoalTracker(
    goals: List<SavingsGoalProgress>,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingsGoal) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(goals) { goalProgress ->
                SavingsGoalItem(
                    goalProgress = goalProgress,
                    onEdit = { onEditGoal(goalProgress.goal) },
                    onDelete = { onDeleteGoal(goalProgress.goal) }
                )
            }
        }

        FloatingActionButton(
            onClick = onAddGoal,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Savings Goal",
                tint = MaterialTheme.colors.onPrimary
            )
        }
    }
}

@Composable
private fun SavingsGoalItem(
    goalProgress: SavingsGoalProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = goalProgress.remainingAmount.coerceIn(0.0, 1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 4.dp
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
                        text = goalProgress.goal.name,
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = "Target: ${formatCurrency(goalProgress.goal.targetAmount)}",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Savings Goal",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Savings Goal",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    progress >= 1f -> MaterialTheme.colors.primary
                    progress >= 0.8f -> MaterialTheme.colors.secondary
                    else -> MaterialTheme.colors.primary
                },
                backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved: ${formatCurrency(goalProgress.remainingAmount)}",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Target: ${formatCurrency(goalProgress.goal.targetAmount)}",
                    style = MaterialTheme.typography.body2
                )
            }

            if (goalProgress.remainingAmount > 0) {
                Text(
                    text = "Remaining: ${formatCurrency(goalProgress.remainingAmount)}",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.primary
                )
            } else {
                Text(
                    text = "Goal Achieved!",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.primary
                )
            }

            goalProgress.goal.targetDate?.let { date ->
                Text(
                    text = "Target Date: $date",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
