package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.esteban.ruano.lifecommander.ui.components.TransactionItem

@Composable
fun BudgetTransactionsScreen(
    budgetId: String,
    onBack: () -> Unit,
    financeViewModel: FinanceViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val budgetTransactions = financeViewModel.state.collectAsState().value.budgetTransactions
    val isLoading = financeViewModel.state.collectAsState().value.isLoading
    val error = financeViewModel.state.collectAsState().value.error

    LaunchedEffect(budgetId) {
        financeViewModel.getBudgetTransactions(budgetId)
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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text(
                text = error!!,
                color = Color.Red,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(budgetTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onTransactionClick = { /* Handle transaction click */ },
                        onEdit = { /* Handle edit */ },
                        onDelete = { /* Handle delete */ }
                    )
                }
            }
        }
    }
}
