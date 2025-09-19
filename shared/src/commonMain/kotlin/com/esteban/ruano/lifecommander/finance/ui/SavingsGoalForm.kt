package com.lifecommander.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.utils.DateUtils.parseDateTime
import com.lifecommander.finance.model.Account
import com.lifecommander.finance.model.SavingsGoal
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun SavingsGoalForm(
    accounts: List<Account>,
    initialGoal: SavingsGoal? = null,
    onSave: (SavingsGoal) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialGoal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(initialGoal?.targetAmount?.toString() ?: "") }
    var currentAmount by remember { mutableStateOf(initialGoal?.currentAmount?.toString() ?: "0.0") }
    var targetDate by remember {
        mutableStateOf(
            initialGoal?.targetDate ?: Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .plus(DatePeriod(months = 12))
                .atTime(LocalTime(0, 0)).parseDateTime()
        )
    }

    var accountId by remember { mutableStateOf(initialGoal?.accountId ?: accounts.firstOrNull()?.id ?: "") }
    var monthlyContribution by remember { mutableStateOf(initialGoal?.monthlyContribution?.toString() ?: "0.0") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (initialGoal == null) "Add Savings Goal" else "Edit Savings Goal",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = targetAmount,
            onValueChange = { targetAmount = it },
            label = { Text("Target Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = currentAmount,
            onValueChange = { currentAmount = it },
            label = { Text("Current Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = targetDate.toString(),
            onValueChange = { },
            readOnly = true,
            label = { Text("Target Date") },
            modifier = Modifier.fillMaxWidth()
        )

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
            value = monthlyContribution,
            onValueChange = { monthlyContribution = it },
            label = { Text("Monthly Contribution") },
            modifier = Modifier.fillMaxWidth()
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
                    val goal = SavingsGoal(
                        name = name,
                        id = initialGoal?.id,
                        targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                        currentAmount = currentAmount.toDoubleOrNull() ?: 0.0,
                        targetDate = targetDate,
                        accountId = accountId,
                        monthlyContribution = monthlyContribution.toDoubleOrNull() ?: 0.0
                    )
                    onSave(goal)
                },
                enabled = name.isNotEmpty() && targetAmount.isNotEmpty() && accountId.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
} 