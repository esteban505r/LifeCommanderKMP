package com.lifecommander.finance.ui

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
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.MR
import com.esteban.ruano.lifecommander.finance.ui.components.FormattedAmountInput
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.ui.components.EnumChipSelector
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.parseDate
import com.lifecommander.finance.model.*
import com.lifecommander.models.Frequency
import com.lifecommander.ui.components.CustomDatePicker
import com.lifecommander.ui.components.CustomTimePicker
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.*

@OptIn(ExperimentalMaterialApi::class)
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
    var dateTime by remember {
        mutableStateOf(
            initialTransaction?.date?.toLocalDateTime()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(initialTransaction?.frequency ?: Frequency.MONTHLY) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        FormattedAmountInput(
            amount = amount,
            onAmountChange = { amount = it },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionType.values().forEach { transactionType ->
                FilterChip(
                    selected = type == transactionType,
                    onClick = { type = transactionType },
                    content = { Text(transactionType.name.lowercase()) }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(MR.strings.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                Category.entries.forEach { transactionCategory ->
                    DropdownMenuItem(
                        onClick = {
                            category = transactionCategory
                            categoryExpanded = false
                        }
                    ) {
                        Text(
                            transactionCategory.name.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = accountExpanded,
            onExpandedChange = { accountExpanded = it }
        ) {
            OutlinedTextField(
                value = accounts.find { it.id == accountId }?.name ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(MR.strings.account)) },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    cursorColor = MaterialTheme.colors.primary,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            )

            ExposedDropdownMenu(
                expanded = accountExpanded,
                onDismissRequest = { accountExpanded = false }
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        onClick = {
                            accountId = account.id ?: ""
                            accountExpanded = false
                        }
                    ) {
                        Text(account.name)
                    }
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(MR.strings.description)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                cursorColor = MaterialTheme.colors.primary,
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        )

        Text("Date and Time", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showDatePicker = true },
            enabled = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = dateTime?.date?.parseDate() ?: "Select a date",
                style = MaterialTheme.typography.body1
            )
        }
        if (showDatePicker) {
            Dialog(
                onDismissRequest = { showDatePicker = false },
            )
            {
                Surface {
                    CustomDatePicker(
                        selectedDate = dateTime?.date ?: getCurrentDateTime(
                            TimeZone.currentSystemDefault()
                        ).date,
                        onDateSelected = { dateTime = it.atTime(dateTime?.time ?: LocalTime(0, 0)) },
                        modifier = Modifier.fillMaxWidth(),
                        onDismiss = { showDatePicker = false }
                    )
                }
            }
        }
        OutlinedButton(
            onClick = { showTimePicker = true },
            enabled = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = dateTime?.time?.formatDefault() ?: "Select a time",
                style = MaterialTheme.typography.body1
            )
        }
        if (showTimePicker) {
            Dialog(
                onDismissRequest = { showTimePicker = false },
            )
            {
                Surface {
                    CustomTimePicker(
                        selectedTime = dateTime?.time ?: LocalTime(0, 0),
                        onTimeSelected = { dateTime = dateTime?.date?.atTime(it) },
                        modifier = Modifier.fillMaxWidth(),
                        onDismiss = { showTimePicker = false }
                    )
                }
            }
        }

        EnumChipSelector(
            enumValues = Frequency.entries.toTypedArray(),
            selectedValue = frequency,
            onValueSelected = { frequency = it ?: Frequency.MONTHLY },
            modifier = Modifier.fillMaxWidth(),
            labelMapper = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colors.primary
                )
            ) {
                Text(stringResource(MR.strings.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (dateTime == null) {
                        onCancel()
                        return@Button
                    }
                    val transaction = Transaction(
                        id = initialTransaction?.id,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        type = type,
                        category = category,
                        accountId = accountId,
                        description = description,
                        date = dateTime!!.formatDefault(),
                        frequency = frequency,
                    )
                    onSave(transaction)
                },
                enabled = amount.isNotEmpty() && accountId.isNotEmpty() && description.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(stringResource(MR.strings.save))
            }
        }
    }
} 