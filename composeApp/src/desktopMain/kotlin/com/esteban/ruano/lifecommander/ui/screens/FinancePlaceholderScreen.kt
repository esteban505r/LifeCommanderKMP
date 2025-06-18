package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.PlaceholderScreen

@Composable
fun FinancePlaceholderScreen(
    modifier: Modifier = Modifier
) {
    PlaceholderScreen(
        title = "Finance Management",
        description = "Track your expenses, manage budgets, and analyze your spending patterns. This feature will help you take control of your financial health.",
        icon = Icons.Default.AccountBalance,
        modifier = modifier
    )
}

@Composable
fun FinanceImporterPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    PlaceholderScreen(
        title = "Transaction Import",
        description = "Import your bank statements and credit card transactions to automatically categorize and track your spending.",
        icon = Icons.Default.Upload,
        modifier = modifier
    )
}

@Composable
fun BudgetTransactionsPlaceholderScreen(
    budgetId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Budget Transactions",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        PlaceholderScreen(
            title = "Budget Transactions",
            description = "View and manage transactions for your budget. Track spending against your budget limits and analyze your financial patterns.",
            icon = Icons.Default.Receipt,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CategoryKeywordMapperPlaceholderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Category Mapper",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        PlaceholderScreen(
            title = "Category Keyword Mapper",
            description = "Automatically categorize your transactions by setting up keyword rules. This will help you organize your finances more efficiently.",
            icon = Icons.Default.Category,
            modifier = Modifier.fillMaxSize()
        )
    }
} 