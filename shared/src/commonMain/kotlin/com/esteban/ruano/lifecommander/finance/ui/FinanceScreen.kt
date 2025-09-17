package com.lifecommander.finance.ui

import BudgetScreenWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.finance.ui.AccountForm
import com.esteban.ruano.lifecommander.finance.ui.components.TransactionListWrapper
import com.esteban.ruano.lifecommander.finance.ui.components.ScheduledTransactionListWrapper
import com.esteban.ruano.lifecommander.ui.state.FinanceState
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.lifecommander.finance.model.*
import com.lifecommander.finance.ui.components.*
import kotlinx.coroutines.launch
import com.esteban.ruano.lifecommander.finance.ui.components.FinanceTabRow
import com.esteban.ruano.lifecommander.finance.ui.components.FinanceTabItem
import com.esteban.ruano.lifecommander.ui.components.AppBar

@Composable
fun FinanceScreen(
    state: FinanceState,
    actions: FinanceActions,
    onOpenImporter: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenBudgetTransactions: (String,String) -> Unit = {_,_ ->},
    onOpenCategoryKeywordMapper: () -> Unit = {},
    isDesktop: Boolean = false
) {
    val selectedTab = state.selectedTab
    var showTransactionForm by remember { mutableStateOf(false) }
    var showScheduledTransactionForm by remember { mutableStateOf(false) }
    var showSavingsGoalForm by remember { mutableStateOf(false) }
    var showAccountForm by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var editingSavingsGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var editingScheduledTransaction by remember { mutableStateOf<ScheduledTransaction?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val scope = rememberCoroutineScope()

    val tabItems = remember {
        listOf(
            FinanceTabItem(
                title = "Accounts",
                icon = {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (selectedTab == 0) 
                            MaterialTheme.colors.primary
                        else 
                            MaterialTheme.colors.primary.copy(alpha = 0.6f)
                    )
                },
                action = { actions.getAccounts() }
            ),
            FinanceTabItem(
                title = "Transactions",
                icon = {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = if (selectedTab == 1) 
                            MaterialTheme.colors.primary
                        else 
                            MaterialTheme.colors.primary.copy(alpha = 0.6f)
                    )
                },
                action = { actions.getTransactions(true) }
            ),
            FinanceTabItem(
                title = "Scheduled",
                icon = {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (selectedTab == 2) 
                            MaterialTheme.colors.primary
                        else 
                            MaterialTheme.colors.primary.copy(alpha = 0.6f)
                    )
                },
                action = { actions.getScheduledTransactions(true) }
            ),
            FinanceTabItem(
                title = "Budgets",
                icon = {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = null,
                        tint = if (selectedTab == 3) 
                            MaterialTheme.colors.primary
                        else 
                            MaterialTheme.colors.primary.copy(alpha = 0.6f)
                    )
                },
                action = { actions.getBudgets() }
            )
        )
    }

    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        modifier = modifier,
        /*topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Finance",
                        color = MaterialTheme.colors.onPrimary
                    )
                },
                backgroundColor = MaterialTheme.colors.primary,
                actions = {
                    IconButton(onClick = {
                        onOpenImporter()
                    }) {
                        Icon(
                            Icons.Default.ImportExport,
                            contentDescription = "Importer",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                    IconButton(onClick = { showTransactionForm = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            )
        }*/
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colors.background)
        ) {
            AppBar("Finance", actions = {
                IconButton(onClick = {
                   onOpenImporter()
                }) {
                    Icon(
                        Icons.Default.ImportExport,
                        contentDescription = "Importer"
                    )
                }
            })
                FinanceTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { tabIndex ->
                        actions.setSelectedTab(tabIndex)
                    },
                    actions = tabItems,
                    isDesktop = isDesktop
                )

                when (selectedTab) {
                    0 -> AccountList(
                        accounts = state.accounts,
                        selectedAccount = state.selectedAccount,
                        onAccountSelected = { scope.launch { actions.selectAccount(it) } },
                        onAddAccount = { showAccountForm = true },
                        onEditAccount = { editingAccount = it },
                        onDeleteAccount = {
                            scope.launch {
                                it.id?.let { id ->
                                    actions.deleteAccount(
                                        id
                                    )
                                }
                            }
                        }
                    )

                    1 -> TransactionListWrapper(
                        transactions = state.transactions,
                        onTransactionClick = { /* Handle transaction click */ },
                        onEdit = { editingTransaction = it },
                        onAddTransaction = { showTransactionForm = true },
                        onDelete = { scope.launch { it.id?.let { id -> actions.deleteTransaction(id) } } },
                        totalCount = state.totalTransactions,
                        onLoadMore = {
                            scope.launch {
                                actions.getTransactions(refresh = false)
                            }
                        },
                        onFiltersChange = {
                            scope.launch {
                                actions.changeTransactionFilters(it, onSuccess = {
                                    actions.getTransactions(refresh = true)
                                })
                            }
                        },
                        isLoading = state.isLoadingTransactions,
                        currentFilters = state.transactionFilters,
                    )

                    2 -> ScheduledTransactionListWrapper(
                        transactions = state.scheduledTransactions,
                        onTransactionClick = { /* Handle scheduled transaction click */ },
                        onEdit = { editingScheduledTransaction = it },
                        onAddTransaction = { showScheduledTransactionForm = true },
                        onDelete = {
                            scope.launch {
                                it.id?.let { id ->
                                    actions.deleteScheduledTransaction(
                                        id
                                    )
                                }
                            }
                        },
                        totalCount = state.totalScheduledTransactions,
                        onLoadMore = {
                            scope.launch {
                                actions.getScheduledTransactions(refresh = false)
                            }
                        },
                        onFiltersChange = {
                            scope.launch {
                                actions.changeTransactionFilters(it, onSuccess = {
                                    actions.getScheduledTransactions(refresh = true)
                                })
                            }
                        },
                        currentFilters = state.transactionFilters,
                    )

                    3 -> {
                        BudgetScreenWrapper(
                            budgets = state.budgets,
                            onLoadBudgets = {
                                actions.getBudgets()
                            },
                            onAddBudget = {
                                actions.addBudget(it)
                            },
                            onEditBudget = {
                                actions.updateBudget(it)
                            },
                            onDeleteBudget = {
                                scope.launch {
                                    it.id?.let { id ->
                                        actions.deleteBudget(
                                            id
                                        )
                                    }
                                }
                            },
                            onBudgetClick = { budget ->
                                onOpenBudgetTransactions(budget.id ?: "", budget.name)
                            },
                            onFiltersChange = {
                                actions.changeBudgetFilters(it)
                            },
                            onChangeBaseDate = {
                                actions.changeBudgetBaseDate(it)
                            },
                            baseDate = state.budgetBaseDate.toLocalDate(),
                            onOpenCategoryKeywordMapper = {
                                onOpenCategoryKeywordMapper()
                            },
                            onCategorizeUnbudgeted = {
                                actions.categorizeUnbudgeted()
                            },
                            onCategorizeAll = {
                                actions.categorizeAll()
                            }
                        )
                    }
                }
            }
    }

    if (showTransactionForm || editingTransaction != null) {
        AlertDialog(
            onDismissRequest = {
                showTransactionForm = false
                editingTransaction = null
            },
            title = {
                Text(
                    if (editingTransaction == null) "New Transaction" else "Edit Transaction",
                    color = MaterialTheme.colors.onSurface
                )
            },
            text = {
                TransactionForm(
                    accounts = state.accounts,
                    initialTransaction = editingTransaction,
                    onSave = { transaction ->
                        scope.launch {
                            if (editingTransaction != null) {
                                actions.updateTransaction(transaction)
                            } else {
                                actions.addTransaction(transaction)
                            }
                            showTransactionForm = false
                            editingTransaction = null
                        }
                    },
                    onCancel = {
                        showTransactionForm = false
                        editingTransaction = null
                    }
                )
            },
            confirmButton = {},
            dismissButton = {},
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    if (showScheduledTransactionForm || editingScheduledTransaction != null) {
        AlertDialog(
            onDismissRequest = {
                showScheduledTransactionForm = false
                editingScheduledTransaction = null
            },
            title = {
                Text(
                    if (editingScheduledTransaction == null) "New Scheduled Transaction" else "Edit Scheduled Transaction",
                    color = MaterialTheme.colors.onSurface
                )
            },
            text = {
                ScheduledTransactionForm(
                    accounts = state.accounts,
                    initialTransaction = editingScheduledTransaction,
                    onSave = { transaction ->
                        scope.launch {
                            val scheduledTransaction = transaction
                            if (editingScheduledTransaction != null) {
                                actions.updateScheduledTransaction(scheduledTransaction)
                            } else {
                                actions.addScheduledTransaction(scheduledTransaction)
                            }
                            showScheduledTransactionForm = false
                            editingScheduledTransaction = null
                        }
                    },
                    onCancel = {
                        showScheduledTransactionForm = false
                        editingScheduledTransaction = null
                    }
                )
            },
            confirmButton = {},
            dismissButton = {},
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    if (showSavingsGoalForm || editingSavingsGoal != null) {
        AlertDialog(
            onDismissRequest = {
                showSavingsGoalForm = false
                editingSavingsGoal = null
            },
            title = {
                Text(
                    if (editingSavingsGoal == null) "Add Savings Goal" else "Edit Savings Goal",
                    color = MaterialTheme.colors.onSurface
                )
            },
            text = {
                SavingsGoalForm(
                    accounts = state.accounts,
                    initialGoal = editingSavingsGoal,
                    onSave = { goal ->
                        scope.launch {
                            if (editingSavingsGoal != null) {
                                actions.updateSavingsGoal(goal)
                            } else {
                                actions.addSavingsGoal(goal)
                            }
                            showSavingsGoalForm = false
                            editingSavingsGoal = null
                        }
                    },
                    onCancel = {
                        showSavingsGoalForm = false
                        editingSavingsGoal = null
                    }
                )
            },
            confirmButton = {},
            dismissButton = {},
            backgroundColor = MaterialTheme.colors.surface
        )
    }

    if (showAccountForm || editingAccount != null) {
        Dialog(
            onDismissRequest = {
                showAccountForm = false
                editingAccount = null
            },
        ) {
            Column(
                modifier = Modifier.background(MaterialTheme.colors.surface).padding(32.dp)
            ) {
                Text(
                    if (editingAccount == null) "Add Account" else "Edit Account",
                    color = MaterialTheme.colors.onSurface,
                )
                AccountForm(
                    initialAccount = editingAccount,
                    onSave = { account ->
                        scope.launch {
                            if (editingAccount != null) {
                                actions.updateAccount(account)
                            } else {
                                actions.addAccount(account)
                            }
                            showAccountForm = false
                            editingAccount = null
                        }
                    },
                    onCancel = {
                        showAccountForm = false
                        editingAccount = null
                    }
                )
            }
        }
    }
} 