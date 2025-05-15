package com.lifecommander.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifecommander.finance.model.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionForm(
    accounts: List<Account>,
    initialTransaction: Transaction? = null,
    onSave: (Transaction) -> Unit,
    onCancel: () -> Unit
) {
    var amount by remember { mutableStateOf(initialTransaction?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(initialTransaction?.category ?: Category.OTHER) }
    var accountId by remember { mutableStateOf(initialTransaction?.accountId ?: accounts.firstOrNull()?.id ?: "") }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var isRecurring by remember { mutableStateOf(initialTransaction?.isRecurring ?: false) }
    var recurrence by remember { mutableStateOf(initialTransaction?.recurrence ?: Recurrence.MONTHLY) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (initialTransaction == null) "Add Transaction" else "Edit Transaction",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionType.values().forEach { transactionType ->
                FilterChip(
                    selected = type == transactionType,
                    onClick = { type = transactionType },
                    label = { Text(transactionType.name) }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = category.name,
                onValueChange = { },
                readOnly = true,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = accounts.find { it.id == accountId }?.name ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Account") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isRecurring,
                onCheckedChange = { isRecurring = it }
            )
            Text("Recurring Transaction")
        }

        if (isRecurring) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Recurrence.values().forEach { recurrenceType ->
                    FilterChip(
                        selected = recurrence == recurrenceType,
                        onClick = { recurrence = recurrenceType },
                        label = { Text(recurrenceType.name) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val transaction = Transaction(
                        id = initialTransaction?.id,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        type = type,
                        category = category,
                        accountId = accountId,
                        description = description,
                        date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        isRecurring = isRecurring,
                        recurrence = if (isRecurring) recurrence else null
                    )
                    onSave(transaction)
                },
                enabled = amount.isNotEmpty() && accountId.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
} 