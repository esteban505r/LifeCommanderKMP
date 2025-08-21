package com.lifecommander.finance.ui.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.utils.toCurrencyFormat
import com.esteban.ruano.utils.DateUIUtils.formatCurrency
import com.lifecommander.finance.model.Account
@Composable
fun AccountList(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit,
    modifier: Modifier = Modifier
) {


    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(accounts) { account ->
                AccountItem(
                    account = account,
                    isSelected = account == selectedAccount,
                    onClick = { onAccountSelected(account) },
                    onEdit = { onEditAccount(account) },
                    onDelete = { onDeleteAccount(account) }
                )
            }
        }

        FloatingActionButton(
            onClick = onAddAccount,
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AccountItem(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
            awaitPointerEventScope {

            }
        },
        backgroundColor = if (isSelected) {
            MaterialTheme.colors.primary.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colors.surface
        },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionContainer {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = account.type.name,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = passwordVisible.let {
                            val text = account.balance.toCurrencyFormat()
                            val textNotVisible = StringBuilder("*").repeat(10)
                            if(it){
                                text
                            }
                            else {
                                textNotVisible
                            }
                        },
                        style = MaterialTheme.typography.h5,
                        color = if (account.balance >= 0) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.error
                        }
                    )
                }
            }
            
            Row {
                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        if(passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Password Visibility",
                        tint = MaterialTheme.colors.primary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Account",
                        tint = MaterialTheme.colors.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Account",
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}