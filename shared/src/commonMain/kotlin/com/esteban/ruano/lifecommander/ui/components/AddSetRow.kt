package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AddSetRow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    initialReps: Int = 0,
    label: String = "Reps",
    onValidate: (Int) -> String? = { reps ->
        when {
            reps.toString().isEmpty() -> "Enter a valid number of reps."
            reps <= 0 -> "Reps must be greater than 0."
            else -> null
        }
    },
    onAdd: (reps: Int) -> Unit
) {
    var repsText by remember { mutableStateOf(initialReps.coerceAtLeast(0).toString()) }
    var localError by remember { mutableStateOf<String?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current

    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it.filter(Char::isDigit).take(4) },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                enabled = enabled && !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            Button(
                onClick = {
                    val reps = repsText.toIntOrNull()
                    val msg = if (reps == null) "Enter a valid number of reps." else onValidate(reps)
                    if (msg != null) {
                        localError = msg
                        return@Button
                    }
                    localError = null
                    keyboard?.hide()
                    onAdd(reps!!)
                },
                enabled = enabled && !isLoading,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.defaultMinSize(minHeight = 40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add set", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        AnimatedVisibility(visible = localError != null) {
            Column {
                Spacer(Modifier.height(6.dp))
                BannerError(localError ?: "")
            }
        }
    }
}

