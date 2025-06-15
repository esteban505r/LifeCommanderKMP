package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifecommander.models.dashboard.TransactionDTO

@Composable
fun FinanceSummary(
    recentTransactions: List<TransactionDTO>,
    accountBalance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colors.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Balance: $" + String.format("%.2f", accountBalance),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            }
            Divider()
            Text("Recent Transactions", style = MaterialTheme.typography.subtitle1)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentTransactions.take(3).forEach { tx ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (tx.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (tx.isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(tx.title, modifier = Modifier.weight(1f))
                        Text(
                            text = (if (tx.isIncome) "+$" else "-") + String.format("%.2f", kotlin.math.abs(tx.amount)),
                            color = if (tx.isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
                            style = MaterialTheme.typography.body2
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(tx.date, style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
} 