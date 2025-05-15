package com.lifecommander.finance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifecommander.finance.model.Budget
import com.lifecommander.finance.model.BudgetProgress
import com.lifecommander.finance.ui.BudgetForm

@Composable
fun BudgetTracker(
    budgets: List<BudgetProgress>,
    onAddBudget: (Budget) -> Unit,
    onEditBudget: (Budget) -> Unit,
    onDeleteBudget: (Budget) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Budget?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgets",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(budgets) { budgetProgress ->
                    BudgetProgressItem(
                        budgetProgress = budgetProgress,
                        onEdit = { showEditDialog = budgetProgress.budget },
                        onDelete = { onDeleteBudget(budgetProgress.budget) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Budget",
                tint = MaterialTheme.colors.onPrimary
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Budget") },
            text = {
                BudgetForm(
                    onSave = { budget ->
                        onAddBudget(budget)
                        showAddDialog = false
                    },
                    onCancel = { showAddDialog = false }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    showEditDialog?.let { budget ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Edit Budget") },
            text = {
                BudgetForm(
                    initialBudget = budget,
                    onSave = { updatedBudget ->
                        onEditBudget(updatedBudget)
                        showEditDialog = null
                    },
                    onCancel = { showEditDialog = null }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
private fun BudgetProgressItem(
    budgetProgress: BudgetProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (budgetProgress.isOverBudget) {
            MaterialTheme.colors.error.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colors.surface
        },
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
                Column {
                    Text(
                        text = budgetProgress.budget.name,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = budgetProgress.budget.category.name,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${budgetProgress.spent} / ${budgetProgress.budget.amount}",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface
                    )
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = budgetProgress.percentageUsed.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = if (budgetProgress.isOverBudget) {
                    MaterialTheme.colors.error
                } else {
                    MaterialTheme.colors.primary
                },
                backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Remaining: ${budgetProgress.remaining}",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "${budgetProgress.percentageUsed.toInt()}%",
                    style = MaterialTheme.typography.body2,
                    color = if (budgetProgress.isOverBudget) {
                        MaterialTheme.colors.error
                    } else {
                        MaterialTheme.colors.onSurface
                    }
                )
            }
        }
    }
} 