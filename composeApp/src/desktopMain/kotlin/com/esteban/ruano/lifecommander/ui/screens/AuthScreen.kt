package ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.screens.ForgotEmailScreen
import com.esteban.ruano.lifecommander.ui.screens.ForgotNewPasswordScreen
import com.esteban.ruano.lifecommander.ui.screens.ForgotTokenScreen
import org.koin.compose.viewmodel.koinViewModel
import ui.state.AuthState
import ui.ui.viewmodels.AuthViewModel
import ui.ui.viewmodels.ForgotStep

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onAuthenticated: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, password: String) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val isSignUp by authViewModel.isSignUp.collectAsState()
    val isForgetting by authViewModel.isForgettingPassword.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) onAuthenticated()
    }

    if (authState is AuthState.Unauthenticated) {
        val state = authState as AuthState.Unauthenticated
        var step by remember { mutableStateOf(ForgotStep.RequestEmail) }

        // Background gradient for a premium feel
        val gradient = Brush.verticalGradient(
            listOf(
                MaterialTheme.colors.primary.copy(alpha = 0.08f),
                MaterialTheme.colors.background
            )
        )

        Surface(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = Triple(isSignUp, isForgetting, step),
                    transitionSpec = {
                        fadeIn() + slideInVertically { it / 6 } togetherWith
                                fadeOut() + slideOutVertically { -it / 6 }
                    },
                    label = "auth-content"
                ) { (signUp, forgetting, currentStep) ->
                    when {
                        signUp -> SignUpCard(
                            isLoading = state.isLoading,
                            errorMessage = state.errorMessage,
                            onBackToLogin = {
                                authViewModel.setSignUp(false)
                                authViewModel.logout()
                            },
                            onSignUp = { name, email, password ->
                                authViewModel.setLoading()
                                onSignUp(name, email, password)
                            }
                        )

                        forgetting -> when (currentStep) {
                            ForgotStep.RequestEmail -> ForgotEmailCard(
                                email = state.email,
                                isLoading = state.isLoading,
                                errorMessage = state.errorMessage,
                                onEmailChange = authViewModel::updateEmail,
                                onBackToLogin = { authViewModel.setForgettingPassword(false) },
                                onSend = {
                                    authViewModel.setLoading()
                                    authViewModel.forgotPassword()
                                    step = ForgotStep.VerifyPin
                                }
                            )

                            ForgotStep.VerifyPin -> ForgotPinCard(
                                isLoading = state.isLoading,
                                errorMessage = state.errorMessage,
                                onBack = { step = ForgotStep.RequestEmail },
                                onBackToLogin = { authViewModel.setForgettingPassword(false) },
                                onVerify = { pin ->
                                    authViewModel.setLoading()
                                    authViewModel.verifyResetPin(pin)
                                    // If it fails, your VM sets errorMessage; UI stays on this step
                                    step = ForgotStep.SetPassword
                                }
                            )

                            ForgotStep.SetPassword -> ForgotSetPasswordCard(
                                isLoading = state.isLoading,
                                errorMessage = state.errorMessage,
                                onBack = { step = ForgotStep.VerifyPin },
                                onBackToLogin = { authViewModel.setForgettingPassword(false) },
                                onSetPassword = { newPassword ->
                                    authViewModel.setLoading()
                                    authViewModel.resetPassword(newPassword)
                                    // On success, VM clears errors; return to login
                                    authViewModel.setForgettingPassword(false)
                                }
                            )

                        }

                        else -> LoginCard(
                            email = state.email,
                            password = state.password,
                            isLoading = state.isLoading,
                            errorMessage = state.errorMessage,
                            onEmailChange = authViewModel::updateEmail,
                            onPasswordChange = authViewModel::updatePassword,
                            onLogin = { email, password ->
                                authViewModel.setLoading()
                                onLogin(email, password)
                            },
                            onSignUp = { authViewModel.setSignUp(true) },
                            onForgot = { authViewModel.setForgettingPassword(true) }
                        )
                    }
                }
            }
        }
    }
}

/* ---------- Shared card shell with header + stepper ---------- */
@Composable
private fun AuthCard(
    icon: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    step: Int? = null,
    stepCount: Int? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        elevation = 10.dp,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(Modifier.padding(22.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                if (icon != null) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
                }
            }

            if (step != null && stepCount != null) {
                Stepper(step, stepCount, Modifier.padding(top = 12.dp, bottom = 4.dp))
            }

            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun Stepper(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        repeat(total) { i ->
            val active = i < current
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color = if (active) MaterialTheme.colors.primary.copy(alpha = 0.9f)
                        else MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}

/* ---------- Login ---------- */
@Composable
private fun LoginCard(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    onSignUp: () -> Unit,
    onForgot: () -> Unit
) {
    AuthCard(
        icon = { Icon(Icons.Default.Lock, null) },
        title = "Welcome back",
        subtitle = "Log in to continue"
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, null) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colors.error)
        }

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onLogin(email.trim(), password) },
            enabled = !isLoading && email.contains("@") && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isLoading) "Signing in…" else "Sign in") }

        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onForgot, enabled = !isLoading) { Text("Forgot password?") }
            TextButton(onClick = onSignUp, enabled = !isLoading) { Text("Create account") }
        }
    }
}

/* ---------- Sign Up ---------- */
@Composable
private fun SignUpCard(
    isLoading: Boolean,
    errorMessage: String?,
    onBackToLogin: () -> Unit,
    onSignUp: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val canSubmit = !isLoading && name.isNotBlank() && email.contains("@") && password.length >= 8

    AuthCard(
        icon = { Icon(Icons.Default.CheckCircle, null) },
        title = "Create your account",
        subtitle = "It only takes a minute",
        onBack = onBackToLogin
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, enabled = !isLoading, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password (8+ chars)") }, singleLine = true, enabled = !isLoading, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp)); Text(errorMessage, color = MaterialTheme.colors.error)
        }

        Spacer(Modifier.height(14.dp))
        Button(onClick = { onSignUp(name.trim(), email.trim(), password) }, enabled = canSubmit, modifier = Modifier.fillMaxWidth()) {
            Text(if (isLoading) "Creating…" else "Create account")
        }
    }
}

/* ---------- Forgot: Step 1 (Email) ---------- */
@Composable
private fun ForgotEmailCard(
    email: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onBackToLogin: () -> Unit,
    onSend: () -> Unit
) {
    AuthCard(
        icon = { Icon(Icons.Default.Email, null) },
        title = "Reset your password",
        subtitle = "We’ll send a 6-digit code to your email",
        step = 1, stepCount = 3,
        onBack = onBackToLogin
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp)); Text(errorMessage, color = MaterialTheme.colors.error)
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onSend,
            enabled = !isLoading && email.contains("@"),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isLoading) "Sending code…" else "Send code") }

        Spacer(Modifier.height(6.dp))
        Text(
            "Check your inbox (and spam) for a message from us.",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().alpha(0.9f),
            textAlign = TextAlign.Center
        )
    }
}

/* ---------- Forgot: Step 2 (PIN) ---------- */
@Composable
private fun ForgotPinCard(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onVerify: (pin: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val pinClean = pin.filter(Char::isDigit).take(6)
    LaunchedEffect(pin) { if (pin != pinClean) pin = pinClean }
    val canSubmit = !isLoading && pinClean.length == 6

    AuthCard(
        icon = { Icon(Icons.Default.VpnKey, null) },
        title = "Enter the code",
        subtitle = "It’s the 6-digit code from the email",
        step = 2, stepCount = 3,
        onBack = onBack
    ) {
        OutlinedTextField(
            value = pinClean,
            onValueChange = { pin = it },
            label = { Text("6-digit code") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp)); Text(errorMessage, color = MaterialTheme.colors.error)
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onVerify(pinClean) },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isLoading) "Verifying…" else "Verify") }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack, enabled = !isLoading) { Text("Back") }
            TextButton(onClick = onBackToLogin, enabled = !isLoading) { Text("Back to login") }
        }
    }
}

/* ---------- Forgot: Step 3 (New Password) ---------- */
@Composable
private fun ForgotSetPasswordCard(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onSetPassword: (newPassword: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val strong = password.length >= 10
    val match = password == confirm
    val canSubmit = !isLoading && strong && match

    AuthCard(
        icon = { Icon(Icons.Default.Lock, null) },
        title = "Set a new password",
        subtitle = "Make it at least 10 characters",
        step = 3, stepCount = 3,
        onBack = onBack
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("New password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("Confirm password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        val helper = when {
            password.isBlank() && confirm.isBlank() -> null
            !strong -> "Use at least 10 characters."
            !match -> "Passwords don’t match."
            else -> null
        }
        if (!helper.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(helper, color = if (strong && match) Color.Unspecified else MaterialTheme.colors.error)
        }
        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp)); Text(errorMessage, color = MaterialTheme.colors.error)
        }

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onSetPassword(password) },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isLoading) "Saving…" else "Save password") }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack, enabled = !isLoading) { Text("Back") }
            TextButton(onClick = onBackToLogin, enabled = !isLoading) { Text("Back to login") }
        }
    }
}

