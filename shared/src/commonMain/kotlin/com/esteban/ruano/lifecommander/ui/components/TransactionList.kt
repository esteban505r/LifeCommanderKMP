package com.esteban.ruano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.MR
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionType
import dev.icerock.moko.resources.compose.stringResource

fun LazyListScope.transactionListSection(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransaction: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    if (transactions.isEmpty()) return
    
    items(transactions.size) { index ->
        val transaction = transactions[index]
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transaction.category.name,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (transaction.type) {
                            TransactionType.INCOME -> "+$${transaction.amount}"
                            TransactionType.EXPENSE -> "-$${transaction.amount}"
                            TransactionType.TRANSFER -> "$${transaction.amount}"
                        },
                        style = MaterialTheme.typography.subtitle1,
                        color = when (transaction.type) {
                            TransactionType.INCOME -> Color.Green
                            TransactionType.EXPENSE -> Color.Red
                            TransactionType.TRANSFER -> MaterialTheme.colors.onSurface
                        }
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                IconButton(onClick = { onEdit(transaction) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(MR.strings.edit),
                        tint = MaterialTheme.colors.primary
                    )
                }
                
                IconButton(onClick = { onDelete(transaction) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(MR.strings.delete),
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransaction: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        LazyColumn(
            modifier = modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            transactionListSection(
                transactions = transactions,
                onTransactionClick = onTransactionClick,
                onEdit = onEdit,
                onDelete = onDelete,
                onAddTransaction = { }
            )
        }
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Account",
                tint = MaterialTheme.colors.onPrimary
            )
        }
    }
} 