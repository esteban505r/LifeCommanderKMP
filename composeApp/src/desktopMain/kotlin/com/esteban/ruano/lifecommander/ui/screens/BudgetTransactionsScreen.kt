package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.finance.ui.components.TransactionListWrapper
import com.lifecommander.finance.model.FinanceActions
import com.esteban.ruano.lifecommander.ui.state.FinanceState
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.ui.TransactionForm
import kotlinx.coroutines.launch

@Composable
fun BudgetTransactionsScreen(
    budgetId: String,
    onBack: () -> Unit,
    financeActions: FinanceActions,
    modifier: Modifier = Modifier,
    state: FinanceState
) {
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    val error = state.error
    val scope = rememberCoroutineScope()
    var showTransactionForm by remember { mutableStateOf(false) }

    LaunchedEffect(budgetId) {
        financeActions.getBudgetTransactions(budgetId)
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
                text = "Budget Transactions",
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
        )

    }
}
