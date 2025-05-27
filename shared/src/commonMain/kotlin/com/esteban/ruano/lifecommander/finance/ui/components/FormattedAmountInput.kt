package com.esteban.ruano.lifecommander.finance.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun FormattedAmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = amount))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val raw = newValue.text.replace(",", "")

            if (raw.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                val parts = raw.split(".")
                val whole = parts.getOrNull(0)?.toLongOrNull()?.let {
                    it.toString().reversed().chunked(3).joinToString(",").reversed()
                } ?: ""
                val decimals = parts.getOrNull(1)?.take(2)
                val formatted = if (decimals != null) "$whole.$decimals" else whole

                val cursorOffset = formatted.length - raw.length + newValue.selection.end
                val newCursor = cursorOffset.coerceIn(0, formatted.length)

                textFieldValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(newCursor)
                )

                onAmountChange(raw) // emit raw value
            }
        },
        label = { Text("Amount") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.colors.onSurface,
            cursorColor = MaterialTheme.colors.primary,
            focusedBorderColor = MaterialTheme.colors.primary,
            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        )
    )
}