package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.finance.ui.components.TransactionListWrapper
import com.esteban.ruano.lifecommander.ui.state.FinanceState
import com.lifecommander.finance.model.FinanceActions
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.ui.TransactionForm
import kotlinx.coroutines.launch

@Composable
fun BudgetTransactionsScreen(
    budgetId: String,
    budgetName: String,
    onBack: () -> Unit,
    financeActions: FinanceActions,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit,
    state: FinanceState,
    isTransactionsLoading: Boolean = false,
    transactionsError: String? = null,
    onTransactionClick: (Transaction) -> Unit = {},
    onEdit: (Transaction) -> Unit = {},
    onDelete: (Transaction) -> Unit = {}
) {
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()
    var showTransactionForm by remember { mutableStateOf(false) }

    // Trigger for loading transactions
    LaunchedEffect(budgetId) {
        financeActions.getBudgetTransactions(budgetId)
    }

    val listState = rememberLazyListState()

    // Handle pagination
    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                println("TRIGGERING LAUNCHED EFFECT")
                println("$lastVisibleItemIndex")
                println("${state.transactions.size}")
                println(state.transactions)
                if (listState.layoutInfo.totalItemsCount>0 && (lastVisibleItemIndex?:0)>0 && lastVisibleItemIndex != null && lastVisibleItemIndex >= listState.layoutInfo.totalItemsCount - 5) {
                    println("LOADING MORE lastVisibleItem $lastVisibleItemIndex")
                    onLoadMore()
                }
            }
    }

    // Add/Edit Transaction Dialog
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
                                financeActions.updateTransaction(transaction)
                            } else {
                                financeActions.addTransaction(transaction)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = budgetName.let { "Budget Transactions - $it" } ?: "Budget Transactions",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
        }

        TransactionListWrapper(
            transactions = state.transactions,
            onTransactionClick = { /* Handle transaction click */ },
            onEdit = { editingTransaction = it },
            onAddTransaction = { showTransactionForm = true },
            onDelete = { scope.launch { it.id?.let { id -> financeActions.deleteTransaction(id) } } },
            totalCount = state.totalTransactions,
            onLoadMore = {
                scope.launch {
                    financeActions.getTransactions(refresh = false)
                }
            },
            onFiltersChange = {
                scope.launch {
                    financeActions.changeTransactionFilters(it, onSuccess = {
                        financeActions.getBudgetTransactions(budgetId)
                    })
                }
            },
            currentFilters = state.transactionFilters,
            isLoading = isTransactionsLoading
        )
    }
}
