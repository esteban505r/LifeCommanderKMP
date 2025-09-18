package com.esteban.lopez.onboarding_presentation.auth.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.lopez.onboarding_presentation.components.BrandPill
import com.esteban.lopez.onboarding_presentation.components.OrDivider
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.lifecommander.ui.components.button.BaseButton
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthIntent
import com.esteban.ruano.resources.Res
import com.esteban.ruano.resources.logo
import com.esteban.ruano.resources.otter_focused
import com.esteban.ruano.ui.Gray
import com.esteban.ruano.ui.Gray2


@Composable
fun SignUpScreen(
    userIntent: (AuthIntent) -> Unit,
    onBackToLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val sendMainIntent = LocalMainIntent.current
    val context = LocalContext.current

    val isEmailValid = remember(email) { email.contains("@") && email.contains(".") }
    val isPasswordValid = remember(password) { password.length >= 8 }
    val isMatch = remember(password, confirm) { confirm.isNotEmpty() && password == confirm }
    val canSubmit = name.isNotBlank() && isEmailValid && isPasswordValid && isMatch

    // Background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colors.primary.copy(alpha = 0.06f),
                        MaterialTheme.colors.background
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = org.jetbrains.compose.resources.painterResource(
                    Res.drawable.otter_focused
                ),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .size(200.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.height(4.dp))
            Text("Oter", style = MaterialTheme.typography.h1)
            Spacer(Modifier.height(18.dp))

            Card(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {

                    // Brand pill
                    BrandPill(name = "Esteban Ruano")

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Create your account",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "It only takes a minute — let’s get you set up.",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(Modifier.height(18.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Gray2,
                            unfocusedBorderColor = Gray2,
                            placeholderColor = Gray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.email)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Gray2,
                            unfocusedBorderColor = Gray2,
                            placeholderColor = Gray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(id = R.string.password)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Gray2,
                            unfocusedBorderColor = Gray2,
                            placeholderColor = Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Confirm password
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        label = { Text("Confirm password") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    imageVector = if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (confirmVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Gray2,
                            unfocusedBorderColor = Gray2,
                            placeholderColor = Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Validation helper
                    val helper = when {
                        name.isBlank() -> null
                        email.isNotBlank() && !isEmailValid -> "Enter a valid email address."
                        password.isNotBlank() && !isPasswordValid -> "Password must be at least 8 characters."
                        confirm.isNotBlank() && !isMatch -> "Passwords don’t match."
                        else -> null
                    }
                    if (helper != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            helper,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Primary CTA
                    BaseButton(
                        text = "Create account",
                        onClick = {
                            if (canSubmit) {
                                userIntent(AuthIntent.SignUp(name.trim(), email.trim(), password))
                            } else {
                                sendMainIntent(
                                    MainIntent.ShowSnackBar(
                                        message = context.getString(R.string.error_empty_fields),
                                        type = SnackbarType.ERROR
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    OrDivider()

                    Spacer(Modifier.height(10.dp))

                    // Back to login
                    OutlinedButton(
                        onClick = onBackToLogin,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Gray2)
                    ) {
                        Text("I already have an account")
                    }
                }
            }
        }
    }
}
