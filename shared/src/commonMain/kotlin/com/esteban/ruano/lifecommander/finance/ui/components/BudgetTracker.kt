package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.MR
import com.esteban.ruano.lifecommander.models.finance.*
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector
import com.esteban.ruano.lifecommander.ui.components.ExpandableFilterSection
import com.esteban.ruano.lifecommander.ui.components.FilterSidePanel
import com.esteban.ruano.lifecommander.utils.toCurrencyFormat
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUtils.parseDate
import com.lifecommander.finance.ui.BudgetForm
import com.lifecommander.ui.components.CustomDatePicker
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetTracker(
    budgets: List<BudgetProgress>,
    onLoadBudgets: () -> Unit,
    onAddBudget: (Budget) -> Unit,
    onEditBudget: (Budget) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
    onBudgetClick: (Budget) -> Unit,
    onShowFilters: (Boolean) -> Unit,
    onOpenCategoryKeywordMapper: () -> Unit,
    baseDate: LocalDate,
    filters: BudgetFilters = BudgetFilters(),
    onFiltersChange: (BudgetFilters) -> Unit,
    onToggleDatePicker: (Boolean) -> Unit,
) {
    var showBudgetFormDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var searchPattern by remember { mutableStateOf(filters.searchPattern) }



    // Panel principal
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                color = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Search and Filter Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchPattern ?: "",
                            onValueChange = {
                                searchPattern = it
                                onFiltersChange(filters.copy(searchPattern = it.takeIf { it.isNotBlank() }))
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(MR.strings.search_transactions)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = MaterialTheme.colors.surface
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedButton(
                            onClick = { onToggleDatePicker(true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = baseDate.formatDefault(),
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { onShowFilters(true) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (filters != BudgetFilters()) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                                contentColor = if (filters != BudgetFilters()) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary
                            )
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Filters")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onOpenCategoryKeywordMapper,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary
                            )
                        ) {
                            Icon(Icons.Default.Category, contentDescription = "Category Keywords")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Category Keywords")
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgets",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 64.dp),
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
                        onDelete = { onDeleteBudget(budgetProgress.budget) },
                        onBudgetClick = onBudgetClick
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(end = 64.dp)
                .align(Alignment.BottomCenter),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp
        ) {
            Text(
                text = "Total Budgeted: ${budgets.sumOf { it.budget.amount }.toCurrencyFormat()}",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        FloatingActionButton(
            onClick = {
                editingBudget = null
                showBudgetFormDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Budget", tint = MaterialTheme.colors.onPrimary)
        }
    }

    if (showBudgetFormDialog) {
        Dialog(onDismissRequest = { showBudgetFormDialog = false }) {
            Surface {
                BudgetForm(
                    initialBudget = editingBudget,
                    onSave = {
                        if (editingBudget == null) onAddBudget(it)
                        else onEditBudget(it.copy(id = editingBudget!!.id))
                        showBudgetFormDialog = false
                        editingBudget = null
                    },
                    onCancel = { showBudgetFormDialog = false }
                )
            }
        }
    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BudgetProgressItem(
    budgetProgress: BudgetProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBudgetClick: (Budget) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onBudgetClick(budgetProgress.budget)
        },
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
                progress = budgetProgress.progressPercentage.toFloat()/100,
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