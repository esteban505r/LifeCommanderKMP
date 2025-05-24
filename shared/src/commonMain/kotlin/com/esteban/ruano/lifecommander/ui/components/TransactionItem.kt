package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.MR
import com.esteban.ruano.lifecommander.utils.toCurrencyFormat
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionType
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun TransactionItem(
    transaction: Transaction,
    onTransactionClick: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        SelectionContainer {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transaction.category.name,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = transaction.amount.toCurrencyFormat(),
                        style = MaterialTheme.typography.subtitle1,
                        color = when (transaction.type) {
                            TransactionType.INCOME -> Color.Green
                            TransactionType.EXPENSE -> Color.Red
                            TransactionType.TRANSFER -> MaterialTheme.colors.onSurface
                        }
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row {
                    IconButton(onClick = { onEdit(transaction) }) {
                        androidx.compose.material.Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(MR.strings.edit),
                            tint = MaterialTheme.colors.primary
                        )
                    }

                    IconButton(onClick = { onDelete(transaction) }) {
                        androidx.compose.material.Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(MR.strings.delete),
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
        }
    }
}