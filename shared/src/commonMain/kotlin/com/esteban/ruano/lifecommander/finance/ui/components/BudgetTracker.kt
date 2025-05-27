package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.serialization.json.Json

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
    onCategorizeUnbudgeted: () -> Unit,
    onCategorizeAll: () -> Unit,
    baseDate: LocalDate,
    filters: BudgetFilters = BudgetFilters(),
    onFiltersChange: (BudgetFilters) -> Unit,
    onToggleDatePicker: (Boolean) -> Unit,
    isMobile: Boolean = false,
    onShowToolsPanel: () -> Unit = {},
) {
    var showBudgetFormDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var searchPattern by remember { mutableStateOf(filters.searchPattern) }

    LaunchedEffect(Unit) {
        if(!budgets.isEmpty()) {
            println("Loading budgets...")
            println("Budgets loaded: ${Json{}.encodeToString(budgets)}")
        }
    }

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
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(if (isMobile) 8.dp else 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            OutlinedTextField(
                                value = searchPattern ?: "",
                                onValueChange = {
                                    searchPattern = it
                                    onFiltersChange(filters.copy(searchPattern = it.takeIf { it.isNotBlank() }))
                                },
                                placeholder = { Text(stringResource(MR.strings.search_transactions)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = MaterialTheme.colors.surface
                                )
                            )
                            Spacer(modifier = Modifier.height(if (isMobile) 8.dp else 16.dp))
                            OutlinedButton(
                                onClick = { onToggleDatePicker(true) },
                            ) {
                                Text(
                                    text = baseDate.formatDefault(),
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(if (isMobile) 8.dp else 16.dp))
                        Column(
                            verticalArrangement = Arrangement.Center
                        ){
                            Button(
                                onClick = { onShowFilters(true) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (filters != BudgetFilters()) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                                    contentColor = if (filters != BudgetFilters()) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filters")
                            }
                            /*IconButton(onClick = onShowToolsPanel) {
                                Icon(Icons.Default.Menu, contentDescription = "Tools")
                            }*/
                            Button(
                                onClick = onShowToolsPanel,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.surface,
                                    contentColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 128.dp, top = 16.dp),
                contentPadding = PaddingValues(horizontal = if (isMobile) 8.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (isMobile) 8.dp else 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            editingBudget = null
                            showBudgetFormDialog = true
                        },
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Budget",
                                tint = MaterialTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Budget",
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
                items(budgets) { budgetProgress ->
                    BudgetProgressItem(
                        budgetProgress = budgetProgress,
                        onEdit = {
                            editingBudget = budgetProgress.budget
                            showBudgetFormDialog = true
                        },
                        onDelete = { onDeleteBudget(budgetProgress.budget) },
                        onBudgetClick = onBudgetClick,
                        isMobile = isMobile
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isMobile) 8.dp else 16.dp)
                .align(Alignment.BottomCenter),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total Budgeted: ${
                        budgets.filter { it.budget.name != "Unbudgeted" }.sumOf { it.budget.amount }.toCurrencyFormat()
                    }",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).weight(1f)
                )
                Text(
                    text = "Total Overrun: ${
                        budgets.filter { it.budget.name != "Unbudgeted" }
                            .sumOf { if (it.budget.amount < it.spent) (it.spent - it.budget.amount) else 0.0 }
                            .toCurrencyFormat()
                    }",
                    style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.error),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).weight(1f)
                )
                Text(
                    text = "Total Spent: ${
                        budgets.filter { it.budget.name != "Unbudgeted" }.sumOf { it.spent }.toCurrencyFormat()
                    }",
                    style = MaterialTheme.typography.h5,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).weight(1f)
                )
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
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BudgetProgressItem(
    budgetProgress: BudgetProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBudgetClick: (Budget) -> Unit,
    isMobile: Boolean = false
) {
    var showOptionsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    if (!isMobile) {
                        onBudgetClick(budgetProgress.budget)
                    }
                },
                onLongPress = {
                    if (isMobile) {
                        showOptionsDialog = true
                    }
                }
            )
        },
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (budgetProgress.isOverBudget) {
                        MaterialTheme.colors.error.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colors.surface
                    },
                )
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
                    if (!isMobile) {
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = budgetProgress.progressPercentage.toFloat() / 100,
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

    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text(budgetProgress.budget.name) },
            text = { Text("What would you like to do with this budget?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOptionsDialog = false
                        onEdit()
                    }
                ) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showOptionsDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colors.error)
                }
            }
        )
    }
}