package com.esteban.ruano.finance_presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.finance_presentation.ui.viewmodel.AccountViewModel
import com.esteban.ruano.finance_presentation.ui.viewmodel.TransactionViewModel
import com.esteban.ruano.ui.Orange
import com.esteban.ruano.utils.DateUIUtils.formatCurrency
import com.lifecommander.finance.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionImportScreen(
    accountViewModel: AccountViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accountState by accountViewModel.viewState.collectAsState()
    val transactionState by transactionViewModel.viewState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var inputText by remember { mutableStateOf("") }
    var parseError by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf(accountState.accounts.firstOrNull()?.id ?: "") }
    var accountExpanded by remember { mutableStateOf(false) }
    var skipDuplicates by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        accountViewModel.getAccounts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Selection
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = it }
            ) {
                OutlinedTextField(
                    value = accountState.accounts.find { it.id == selectedAccountId }?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    accountState.accounts.forEach { account ->
                        DropdownMenuItem(
                            onClick = {
                                selectedAccountId = account.id!!
                                accountExpanded = false
                            }
                        ) {
                            Text(account.name)
                        }
                    }
                }
            }

            // Transaction Input
            OutlinedTextField(
                value = inputText,
                onValueChange = { 
                    inputText = it
                    parseError = null
                },
                label = { Text("Paste your bank transactions here") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            // Error Display
            if (parseError != null) {
                Text(
                    text = parseError!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.body2
                )
            }

            // Preview Button
            Button(
                onClick = {
                    try {
                        val account = accountState.accounts.find { it.id == selectedAccountId }
                            ?: throw IllegalArgumentException("No account selected")
                        
                        coroutineScope.launch {
                            transactionViewModel.previewTransactionImport(inputText, account.id!!)
                        }
                        parseError = null
                    } catch (e: Exception) {
                        parseError = e.message
                    }
                },
                enabled = selectedAccountId.isNotEmpty() && inputText.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview Transactions")
            }

            // Preview Data
            transactionState.importPreview?.let { preview ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Found ${preview.totalTransactions} transactions (${preview.duplicateCount} duplicates)",
                            style = MaterialTheme.typography.subtitle1
                        )
                        
                        Text(
                            text = "Total amount: ${formatCurrency(preview.totalAmount)}",
                            style = MaterialTheme.typography.subtitle1
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = skipDuplicates,
                                onCheckedChange = { skipDuplicates = it }
                            )
                            Text(
                                text = "Skip duplicate transactions",
                                style = MaterialTheme.typography.body1
                            )
                        }

                        // Preview Transactions List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(preview.items.take(10)) { item ->
                                TransactionPreviewItem(
                                    transaction = item.transaction,
                                    isDuplicate = item.isDuplicate
                                )
                            }
                            if (preview.items.size > 10) {
                                item {
                                    Text(
                                        text = "... and ${preview.items.size - 10} more",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // Import Button
                        Button(
                            onClick = {
                                isImporting = true
                                coroutineScope.launch {
                                    transactionViewModel.importTransactions(
                                            inputText, selectedAccountId, skipDuplicates
                                    )
                                    inputText = ""
                                    isImporting = false
                                    onBack()
                                }
                            },
                            enabled = !isImporting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (isImporting) "Importing..."
                                else "Import Transactions"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionPreviewItem(
    transaction: Transaction,
    isDuplicate: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        backgroundColor = if (isDuplicate) Color.Yellow.copy(alpha = 0.1f) else MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.body2,
                    maxLines = 1
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.body2,
                    color = when (transaction.type) {
                        TransactionType.INCOME -> Color.Green
                        TransactionType.EXPENSE -> Color.Red
                        TransactionType.TRANSFER -> MaterialTheme.colors.primary
                    }
                )
                if (isDuplicate) {
                    Text(
                        text = "Duplicate",
                        style = MaterialTheme.typography.caption,
                        color = Orange
                    )
                }
            }
        }
    }
} 