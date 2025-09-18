package com.esteban.ruano.onboarding_presentation.auth.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.esteban.ruano.ui.Gray
import com.esteban.ruano.ui.Gray2

@Composable
fun LoginScreen(
    userIntent: (AuthIntent) -> Unit,
    onOpenSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val sendMainIntent = LocalMainIntent.current
    val context = LocalContext.current

    val isEmailValid = remember(email) { email.contains("@") && email.contains(".") }
    val isPasswordValid = remember(password) { password.length >= 8 }
    val canSubmit = isEmailValid && isPasswordValid

    // Background gradient that matches the brand
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
                painter = org.jetbrains.compose.resources.painterResource(Res.drawable.logo),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .size(200.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
            )
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
                        text = stringResource(id = R.string.login),
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Welcome back! Please sign in to continue.",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(Modifier.height(18.dp))

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
                            imeAction = ImeAction.Done
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

                    // Forgot password link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            onForgotPassword()
                        }) {
                            Text("Forgot password?")
                        }
                    }

                    // Validation helper
                    val helper = when {
                        email.isNotBlank() && !isEmailValid -> "Enter a valid email address."
                        password.isNotBlank() && !isPasswordValid -> "Password must be at least 8 characters."
                        else -> null
                    }
                    if (helper != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            helper,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Primary CTA
                    BaseButton(
                        text = stringResource(id = R.string.login),
                        onClick = {
                            if (canSubmit) {
                                userIntent(AuthIntent.Login(email.trim(), password))
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

                    // Divider with "or"
                    OrDivider()

                    Spacer(Modifier.height(10.dp))

                    // Secondary CTA – Create account
                    OutlinedButton(
                        onClick = {
                            onOpenSignUp()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Gray2)
                    ) {
                        Text("Create account")
                    }
                }
            }
        }
    }
}

/* ---------- Reusable bits ---------- */



