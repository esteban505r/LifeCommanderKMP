package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.core.qualifier.named
import ui.theme.*

@Composable
fun SignUpScreen(
    onSignUp: (name:String,email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordsMatch by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo or App Name
        Text(
            text = "Life Commander",
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // Sign Up Form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = CardBackground,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name",
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = MaterialTheme.colors.primary,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = DividerColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = MaterialTheme.colors.primary,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = DividerColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                )
                
                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordsMatch = it == confirmPassword
                    },
                    placeholder = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = MaterialTheme.colors.primary,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = DividerColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                )
                
                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        passwordsMatch = it == password
                    },
                    placeholder = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Password",
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = MaterialTheme.colors.primary,
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = DividerColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                )
                
                // Password Match Error
                if (!passwordsMatch) {
                    Text(
                        text = "Passwords do not match",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Error Message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Sign Up Button
                Button(
                    onClick = { onSignUp(name,email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading && 
                            email.isNotBlank() && 
                            password.isNotBlank() && 
                            confirmPassword.isNotBlank() && 
                            passwordsMatch
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Sign Up")
                    }
                }
            }
        }
        
        // Login Link
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "Already have an account? Login",
                color = MaterialTheme.colors.primary
            )
        }
    }
} 