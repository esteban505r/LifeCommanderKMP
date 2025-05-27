package com.esteban.ruano.lifecommander.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifecommander.finance.model.Account
import com.lifecommander.finance.model.AccountType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountForm(
    initialAccount: Account? = null,
    onSave: (Account) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialAccount?.name ?: "") }
    var type by remember { mutableStateOf(initialAccount?.type ?: AccountType.CHECKING) }
    var initialBalance by remember { mutableStateOf(initialAccount?.initialBalance?.toString() ?: "0.00") }
    var balance by remember { mutableStateOf(initialAccount?.balance?.toString() ?: "0.00") }
    var currency by remember { mutableStateOf(initialAccount?.currency ?: "USD") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Account Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Account Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AccountType.values().forEach { accountType ->
                    DropdownMenuItem(
                        onClick = {
                            type = accountType
                            expanded = false
                        }
                    ) {
                        Text(
                            accountType.name.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = initialBalance,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    initialBalance = it
                }
            },
            singleLine = true,
            label = { Text("Initial Balance") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currency,
            onValueChange = { currency = it.uppercase().take(3) },
            maxLines = 1,
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

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
                    val account = Account(
                        id = initialAccount?.id,
                        name = name,
                        type = type,
                        initialBalance = initialBalance.toDoubleOrNull() ?: 0.0,
                        currency = currency,
                        balance = balance.toDoubleOrNull() ?: 0.0,
                    )
                    onSave(account)
                },
                enabled = name.isNotEmpty() && initialBalance.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
} 