package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ForgotNewPasswordScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSetPassword: (token: String, newPassword: String) -> Unit,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var token by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val canSubmit = !isLoading && token.isNotBlank() && password.length >= 10 && password == confirm

    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Set a new password", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(6.dp))
            Text("Your token was verified. Create a strong password.", style = MaterialTheme.typography.body2)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Reset token") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirm password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colors.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onSetPassword(token.trim(), password) },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Saving..." else "Set password")
            }

            Spacer(Modifier.height(8.dp))
            Row {
                TextButton(onClick = onBack, enabled = !isLoading) { Text("Back") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onBackToLogin, enabled = !isLoading) { Text("Back to login") }
            }
        }
    }
}