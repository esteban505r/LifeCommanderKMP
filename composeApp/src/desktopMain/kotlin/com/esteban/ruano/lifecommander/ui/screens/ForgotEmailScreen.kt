package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ForgotEmailScreen(
    email: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Reset your password", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter your email and we’ll send a reset token.",
                style = MaterialTheme.typography.body2
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email address") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colors.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onSend,
                enabled = !isLoading && email.contains("@"),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Sending..." else "Send token")
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBackToLogin, enabled = !isLoading) {
                Text("Back to login")
            }
        }
    }
}

@Composable
fun ForgotTokenScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onVerify: (token: String) -> Unit,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var token by remember { mutableStateOf("") }

    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Enter the token", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(6.dp))
            Text("Paste the reset token we sent to your email.", style = MaterialTheme.typography.body2)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Reset token") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colors.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onVerify(token.trim()) },
                enabled = !isLoading && token.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Verifying..." else "Verify token")
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


