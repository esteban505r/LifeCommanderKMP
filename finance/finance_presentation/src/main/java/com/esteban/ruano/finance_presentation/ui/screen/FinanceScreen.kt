//package com.esteban.ruano.finance_presentation.ui.screen
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.esteban.ruano.core_ui.components.*
//import com.esteban.ruano.core_ui.theme.AppTheme
//import com.esteban.ruano.finance_presentation.ui.intent.FinanceIntent
//import com.esteban.ruano.finance_presentation.ui.viewmodel.FinanceViewModel
//import com.esteban.ruano.finance_presentation.ui.viewmodel.state.FinanceState
//import com.esteban.ruano.finance_presentation.ui.viewmodel.state.FinanceTab
//
//@Composable
//fun FinanceScreen(
//    viewModel: FinanceViewModel = hiltViewModel()
//) {
//    val state by viewModel.state.collectAsState()
//
//    AppTheme {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("Finance") },
//                    actions = {
//                        IconButton(onClick = { /* TODO: Show settings */ }) {
//                            Icon(Icons.Default.Settings, contentDescription = "Settings")
//                        }
//                    }
//                )
//            }
//        ) { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                // Tab Row
//                TabRow(
//                    selectedTabIndex = state.selectedTab.ordinal,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    FinanceTab.values().forEach { tab ->
//                        Tab(
//                            selected = state.selectedTab == tab,
//                            onClick = { /* TODO: Handle tab selection */ },
//                            text = { Text(tab.name) }
//                        )
//                    }
//                }
//
//                // Content based on selected tab
//                when (state.selectedTab) {
//                    FinanceTab.OVERVIEW -> OverviewTab(state)
//                    FinanceTab.TRANSACTIONS -> TransactionsTab(state)
//                    FinanceTab.BUDGETS -> BudgetsTab(state)
//                    FinanceTab.SAVINGS -> SavingsTab(state)
//                    FinanceTab.SCHEDULED -> ScheduledTab(state)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun OverviewTab(state: FinanceState) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Account Summary
//        Card(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text(
//                    text = "Accounts",
//                    style = MaterialTheme.typography.titleLarge
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                state.accounts.forEach { account ->
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(account.name)
//                        Text(account.balance.toString())
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Budget Summary
//        Card(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text(
//                    text = "Budgets",
//                    style = MaterialTheme.typography.titleLarge
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                state.budgets.forEach { budgetWithProgress ->
//                    Column {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(budgetWithProgress.budget.name)
//                            Text("${budgetWithProgress.progress.percentageComplete}%")
//                        }
//                        LinearProgressIndicator(
//                            progress = budgetWithProgress.progress.percentageComplete / 100f,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Savings Goals Summary
//        Card(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text(
//                    text = "Savings Goals",
//                    style = MaterialTheme.typography.titleLarge
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                state.savingsGoals.forEach { goal ->
//                    val progress = state.savingsGoalProgress[goal.id] ?: 0f
//                    Column {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(goal.name)
//                            Text("${progress}%")
//                        }
//                        LinearProgressIndicator(
//                            progress = progress / 100f,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun TransactionsTab(state: FinanceState) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Account Selector
//        DropdownMenu(
//            expanded = false,
//            onDismissRequest = { /* TODO */ },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            state.accounts.forEach { account ->
//                DropdownMenuItem(
//                    text = { Text(account.name) },
//                    onClick = { /* TODO */ }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Transaction List
//        LazyColumn {
//            items(state.transactions) { transaction ->
//                TransactionItem(transaction)
//            }
//        }
//
//        // FAB for adding new transaction
//        FloatingActionButton(
//            onClick = { /* TODO */ },
//            modifier = Modifier
//                .align(Alignment.End)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
//        }
//    }
//}
//
//@Composable
//private fun BudgetsTab(state: FinanceState) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Budget List
//        LazyColumn {
//            items(state.budgets) { budgetWithProgress ->
//                BudgetItem(budgetWithProgress)
//            }
//        }
//
//        // FAB for adding new budget
//        FloatingActionButton(
//            onClick = { /* TODO */ },
//            modifier = Modifier
//                .align(Alignment.End)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "Add Budget")
//        }
//    }
//}
//
//@Composable
//private fun SavingsTab(state: FinanceState) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Savings Goals List
//        LazyColumn {
//            items(state.savingsGoals) { goal ->
//                val progress = state.savingsGoalProgress[goal.id] ?: 0f
//                SavingsGoalItem(goal, progress)
//            }
//        }
//
//        // FAB for adding new savings goal
//        FloatingActionButton(
//            onClick = { /* TODO */ },
//            modifier = Modifier
//                .align(Alignment.End)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "Add Savings Goal")
//        }
//    }
//}
//
//@Composable
//private fun ScheduledTab(state: FinanceState) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Scheduled Transactions List
//        LazyColumn {
//            items(state.scheduledTransactions) { transaction ->
//                ScheduledTransactionItem(transaction)
//            }
//        }
//
//        // FAB for adding new scheduled transaction
//        FloatingActionButton(
//            onClick = { /* TODO */ },
//            modifier = Modifier
//                .align(Alignment.End)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "Add Scheduled Transaction")
//        }
//    }
//}
//
//@Composable
//private fun TransactionItem(transaction: Transaction) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column {
//                Text(transaction.description)
//                Text(
//                    text = transaction.date,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//            Text(transaction.amount.toString())
//        }
//    }
//}
//
//@Composable
//private fun BudgetItem(budgetWithProgress: BudgetWithProgress) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(budgetWithProgress.budget.name)
//                Text("${budgetWithProgress.progress.percentageComplete}%")
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            LinearProgressIndicator(
//                progress = budgetWithProgress.progress.percentageComplete / 100f,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
//
//@Composable
//private fun SavingsGoalItem(goal: SavingsGoal, progress: Float) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(goal.name)
//                Text("${progress}%")
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            LinearProgressIndicator(
//                progress = progress / 100f,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
//
//@Composable
//private fun ScheduledTransactionItem(transaction: ScheduledTransaction) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column {
//                Text(transaction.description)
//                Text(
//                    text = "Next: ${transaction.nextOccurrence}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//            Text(transaction.amount.toString())
//        }
//    }
//}