@file:Suppress("FunctionName")

package com.esteban.lopez.onboarding_presentation.auth.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.esteban.lopez.onboarding_presentation.components.BrandPill
import com.esteban.ruano.lifecommander.ui.components.button.BaseButton
import com.esteban.ruano.onboarding_presentation.auth.state.AuthState
import com.esteban.ruano.resources.Res
import com.esteban.ruano.resources.otter_proud
import com.esteban.ruano.ui.Gray
import com.esteban.ruano.ui.Gray2


private enum class ForgotStep { Email, Pin, NewPassword }

private val SpaceXL = 16.dp
private val SpaceL  = 12.dp
private val SpaceM  = 10.dp
private val SpaceS  = 8.dp
private val SpaceXS = 6.dp

@Composable
fun ForgotPasswordScreen(
    state: AuthState,
    onBackToLogin: () -> Unit,
    onRequestCode: (email: String) -> Unit,
    onVerifyPin: (pin: String) -> Unit,
    onSetPassword: (newPassword: String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onResetEmail:() -> Unit
) {
    // Local editing buffers
    var localEmail by rememberSaveable { mutableStateOf("") }
    var localStep by rememberSaveable { mutableStateOf(ForgotStep.Email) }

    // Derive the canonical step from state
    val derivedStep by remember(state.pendingResetEmail, state.resetPasswordToken) {
        mutableStateOf(
            when {
                state.resetPasswordToken != null -> ForgotStep.NewPassword
                state.pendingResetEmail != null -> ForgotStep.Pin
                else -> ForgotStep.Email
            }
        )
    }

    // Sync local step to derived step whenever state changes
    LaunchedEffect(derivedStep) { localStep = derivedStep }

    // Prefill email once backend accepted it (and lock the field)
    LaunchedEffect(state.pendingResetEmail) {
        state.pendingResetEmail?.let { accepted ->
            if (accepted.isNotBlank()) {
                localEmail = accepted
            }
        }
    }

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
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = org.jetbrains.compose.resources.painterResource(Res.drawable.otter_proud),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(SpaceS))
            Text("Oter", style = MaterialTheme.typography.h1)
            Spacer(Modifier.height(SpaceL))

            Card(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(SpaceL)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceS)
                    ) {
                        BrandPill(name = "Esteban Ruano")
                        Spacer(Modifier.weight(1f))
                    }

                    when (localStep) {
                        ForgotStep.Email -> StepEmail(
                            email = localEmail,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onEmailChange = { localEmail = it },
                            onSubmit = {
                                onRequestCode(localEmail.trim())
                            },
                            onBackToLogin = onBackToLogin
                        )

                        ForgotStep.Pin -> StepPin(
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onBack = {
                                // If you want to allow going back, also clear pendingResetEmail at VM if desired
                                localStep = ForgotStep.Email
                                onResetEmail()
                            },
                            onBackToLogin = onBackToLogin,
                            onSubmit = { pin ->
                                onVerifyPin(pin)
                                // VM will set resetPasswordToken → advances automatically
                            }
                        )

                        ForgotStep.NewPassword -> StepNewPassword(
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onBack = {
                                // Allow stepping back to PIN (optional)
                                localStep = ForgotStep.Email
                                onResetEmail()
                            },
                            onBackToLogin = onBackToLogin,
                            onSubmit = { pwd -> onSetPassword(pwd) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(SpaceL))
        }
    }
}

@Composable
private fun StepEmail(
    email: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpaceL)) {
        Column(verticalArrangement = Arrangement.spacedBy(SpaceXS)) {
            Text("Reset your password", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
            Text(
                "Enter your account email and we’ll send you a 6-digit code.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, null) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(errorMessage, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
        }

        BaseButton(
            text = if (isLoading) "Sending…" else "Send code",
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth()
        )

        // Less weight than an outlined button → visually tighter
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(onClick = onBackToLogin) { Text("Back to login") }
        }
    }
}

/* ---------- Step 2: PIN ---------- */

@Composable
private fun StepPin(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val pinClean = remember(pin) { pin.filter(Char::isDigit).take(6) }
    val canSubmit = pinClean.length == 6 && !isLoading

    Column(verticalArrangement = Arrangement.spacedBy(SpaceL)) {
        Column(verticalArrangement = Arrangement.spacedBy(SpaceXS)) {
            Text("Enter the code", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
            Text(
                "We sent a 6-digit code to your email. Enter it to continue.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }

        OutlinedTextField(
            value = pinClean,
            onValueChange = { pin = it },
            label = { Text("6-digit code") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.VpnKey, null) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(errorMessage, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
        }

        BaseButton(
            text = if (isLoading) "Verifying…" else "Verify",
            onClick = { onSubmit(pinClean) },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Back") }
            TextButton(onClick = onBackToLogin) { Text("Back to login") }
        }
    }
}

/* ---------- Step 3: New password ---------- */

@Composable
private fun StepNewPassword(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val strong = password.length >= 10
    val match = password == confirm
    val canSubmit = strong && match && !isLoading

    Column(verticalArrangement = Arrangement.spacedBy(SpaceL)) {
        Column(verticalArrangement = Arrangement.spacedBy(SpaceXS)) {
            Text("Set a new password", style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
            Text(
                "Use at least 10 characters for better security.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("New password") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { show = !show }) {
                    Icon(if (show) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
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

        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("Confirm password") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirm = !showConfirm }) {
                    Icon(if (showConfirm) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
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

        val helper = when {
            password.isNotBlank() && !strong -> "Use at least 10 characters."
            confirm.isNotBlank() && !match -> "Passwords don’t match."
            else -> null
        }
        if (!helper.isNullOrBlank()) {
            Text(helper, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
        }
        if (!errorMessage.isNullOrBlank()) {
            Text(errorMessage, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
        }

        BaseButton(
            text = if (isLoading) "Save password" else "Save password",
            onClick = { onSubmit(password) },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Back") }
            TextButton(onClick = onBackToLogin) { Text("Back to login") }
        }
    }
}
