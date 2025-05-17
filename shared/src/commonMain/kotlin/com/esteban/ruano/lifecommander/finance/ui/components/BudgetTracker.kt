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
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.utils.toCurrencyFormat
import com.lifecommander.finance.ui.BudgetForm

@Composable
fun BudgetTracker(
    budgets: List<BudgetProgress>,
    onAddBudget: (Budget) -> Unit,
    onEditBudget: (Budget) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
) {

    var showBudgetFormDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(budgets) { budgetProgress ->
                    BudgetProgressItem(
                        budgetProgress = budgetProgress,
                        onEdit = {
                            editingBudget = budgetProgress.budget
                            showBudgetFormDialog = true
                        },
                        onDelete = { onDeleteBudget(budgetProgress.budget) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showBudgetFormDialog = true },
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

    if (showBudgetFormDialog) {
        Dialog(
            onDismissRequest = { showBudgetFormDialog = false }) {
            Surface {
                BudgetForm(
                    initialBudget = editingBudget,
                    onSave = { budget ->
                        if (editingBudget == null) {
                            onAddBudget(budget)
                        } else {
                            onEditBudget(budget)
                        }
                        showBudgetFormDialog = false
                        editingBudget = null
                    },
                    onCancel = { showBudgetFormDialog = false },
                )
            }
        }
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
                        text = "${budgetProgress.spent.toCurrencyFormat()} / ${budgetProgress.budget.amount.toCurrencyFormat()}",
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
                progress = budgetProgress.progressPercentage.toFloat(),
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
                    text = "Remaining: ${budgetProgress.remaining.toCurrencyFormat()}",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "${budgetProgress.progressPercentage.toInt()}%",
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