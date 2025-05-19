package com.lifecommander.finance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.finance.ui.AccountForm
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.ui.components.TransactionList
import com.lifecommander.finance.model.*
import com.lifecommander.finance.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun FinanceScreen(
    state: FinanceState,
    actions: FinanceActions,
    onOpenImporter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showTransactionForm by remember { mutableStateOf(false) }
    var showSavingsGoalForm by remember { mutableStateOf(false) }
    var showAccountForm by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var editingSavingsGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            actions.loadData()
        }
    }

    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        modifier = modifier,
        topBar = {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colors.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colors.onPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        coroutineScope.launch {
                            actions.getAccounts()
                        }
                    },
                    text = {
                        Text(
                            "Accounts",
                            color = if (selectedTab == 0) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = if (selectedTab == 0) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onPrimary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        coroutineScope.launch {
                            actions.getTransactions()
                        }
                    },
                    text = {
                        Text(
                            "Transactions",
                            color = if (selectedTab == 1) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = if (selectedTab == 1) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onPrimary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        coroutineScope.launch {
                            actions.getBudgets()
                        }
                    },
                    text = {
                        Text(
                            "Budgets",
                            color = if (selectedTab == 2) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = null,
                            tint = if (selectedTab == 2) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onPrimary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        coroutineScope.launch {
                            actions.getSavingsGoals()
                        }
                    },
                    text = {
                        Text(
                            "Savings",
                            color = if (selectedTab == 3) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (selectedTab == 3) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onPrimary.copy(
                                alpha = 0.6f
                            )
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onPrimary,
                    unselectedContentColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.6f)
                )
            }

            when (selectedTab) {
                0 -> AccountList(
                    accounts = state.accounts,
                    selectedAccount = state.selectedAccount,
                    onAccountSelected = { scope.launch { actions.selectAccount(it) } },
                    onAddAccount = { showAccountForm = true },
                    onEditAccount = { editingAccount = it },
                    onDeleteAccount = { scope.launch { it.id?.let { id -> actions.deleteAccount(id) } } }
                )

                1 -> TransactionList(
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
                            actions.changeTransactionFilters(it)
                        }
                    },
                    currentFilters = state.transactionFilters,
                )

                2 -> BudgetTracker(
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
                    onDeleteBudget = { scope.launch { it.id?.let { id -> actions.deleteBudget(id) } } },
                )

                3 -> SavingsGoalTracker(
                    goals = state.savingsGoals.map { state.savingsGoalProgress[it.id] ?: SavingsGoalProgress(it) },
                    onAddGoal = { showSavingsGoalForm = true },
                    onEditGoal = { editingSavingsGoal = it },
                    onDeleteGoal = { scope.launch { it.id?.let { id -> actions.deleteSavingsGoal(id) } } }
                )
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