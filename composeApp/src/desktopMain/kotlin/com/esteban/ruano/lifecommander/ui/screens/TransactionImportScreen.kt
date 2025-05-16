package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.esteban.ruano.utils.DateUIUtils.formatCurrency
import com.lifecommander.finance.model.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionImportScreen(
    financeViewModel: FinanceViewModel = koinViewModel(),
    onImportComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts = financeViewModel.state.collectAsState().value.accounts
    var inputText by remember { mutableStateOf("") }
    var previewData = financeViewModel.state.collectAsState().value.importPreview
    var parseError by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var accountExpanded by remember { mutableStateOf(false) }
    var skipDuplicates by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        financeViewModel.getAccounts()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = accountExpanded,
            onExpandedChange = { accountExpanded = it }
        ) {
            OutlinedTextField(
                value = accounts.find { it.id == selectedAccountId }?.name ?: "",
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
                accounts.forEach { account ->
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

        OutlinedTextField(
            value = inputText,
            onValueChange = { 
                inputText = it
                parseError = null
                previewData = null
            },
            label = { Text("Paste your bank transactions here") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        if (parseError != null) {
            Text(
                text = parseError!!,
                color = Color.Red,
                style = MaterialTheme.typography.body2
            )
        }

        Button(
            onClick = {
                try {
                    val account = accounts.find { it.id == selectedAccountId }
                        ?: throw IllegalArgumentException("No account selected")
                    
                    financeViewModel.previewTransactionImport(inputText, account.id!!)
                    parseError = null
                } catch (e: Exception) {
                    parseError = e.message
                    previewData = null
                }
            },
            enabled = selectedAccountId.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Preview & Import")
        }

        previewData?.let { preview ->
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(preview.items) { item ->
                    TransactionPreviewItem(
                        transaction = item.transaction,
                        isDuplicate = item.isDuplicate
                    )
                }
            }

            Button(
                onClick = {
                    isImporting = true
                    financeViewModel.importTransactions(inputText, selectedAccountId, skipDuplicates)
                    inputText = ""
                    previewData = null
                    onImportComplete()
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

@Composable
private fun TransactionPreviewItem(
    transaction: Transaction,
    isDuplicate: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.subtitle1
                )
                if (isDuplicate) {
                    Text(
                        text = "Duplicate",
                        color = Color.Red,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            Text(
                text = transaction.date,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.body1,
                color = if (transaction.amount < 0) Color.Red else Color.Green
            )
        }
    }
}
