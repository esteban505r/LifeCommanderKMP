package ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.screens.ForgotEmailScreen
import com.esteban.ruano.lifecommander.ui.screens.ForgotNewPasswordScreen
import com.esteban.ruano.lifecommander.ui.screens.ForgotTokenScreen
import org.koin.compose.viewmodel.koinViewModel
import ui.state.AuthState
import ui.ui.viewmodels.AuthViewModel
import ui.ui.viewmodels.ForgotStep

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onAuthenticated: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (name:String,email: String, password: String) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val isSignUp by authViewModel.isSignUp.collectAsState()
    val isForgottingPassword by authViewModel.isForgettingPassword.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) onAuthenticated()
    }

    if (authState is AuthState.Unauthenticated) {
        val state = authState as AuthState.Unauthenticated
        val errorMessage = state.errorMessage

        // You can lift this into the ViewModel if you prefer:
        var localStep by remember { mutableStateOf(ForgotStep.RequestEmail) }
        val step = localStep

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                when {
                    isSignUp -> SignUpScreen(
                        onSignUp = { name, email, password ->
                            authViewModel.setLoading()
                            onSignUp(name, email, password)
                        },
                        onNavigateToLogin = {
                            authViewModel.setSignUp(false)
                            authViewModel.logout()
                        },
                        isLoading = state.isLoading,
                        errorMessage = errorMessage
                    )

                    isForgottingPassword -> when (step) {
                        ForgotStep.RequestEmail -> ForgotEmailScreen(
                            email = state.email,
                            isLoading = state.isLoading,
                            errorMessage = errorMessage,
                            onEmailChange = authViewModel::updateEmail,
                            onSend = {
                                authViewModel.setLoading()
                                authViewModel.forgotPassword()
                                // Show next step once call returns (success assumed; backend returns 200 always)
                                localStep = ForgotStep.VerifyToken
                            },
                            onBackToLogin = { authViewModel.setForgettingPassword(false) }
                        )

                        ForgotStep.VerifyToken -> ForgotTokenScreen(
                            isLoading = state.isLoading,
                            errorMessage = errorMessage,
                            onVerify = { token ->
                                authViewModel.setLoading()
                                // Call the verify endpoint via service
                                authViewModel.verifyResetToken(token) // implement in VM to call service
                                // If success, advance:
                                localStep = ForgotStep.SetPassword
                            },
                            onBack = { localStep = ForgotStep.RequestEmail },
                            onBackToLogin = { authViewModel.setForgettingPassword(false) }
                        )

                        ForgotStep.SetPassword -> ForgotNewPasswordScreen(
                            isLoading = state.isLoading,
                            errorMessage = errorMessage,
                            onSetPassword = { token, newPassword ->
                                authViewModel.setLoading()
                                authViewModel.resetPassword(token, newPassword)
                                // On success, return to login
                                authViewModel.setForgettingPassword(false)
                            },
                            onBack = { localStep = ForgotStep.VerifyToken },
                            onBackToLogin = { authViewModel.setForgettingPassword(false) }
                        )
                    }

                    else -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        LoginScreen(
                            email = state.email,
                            password = state.password,
                            onEmailChange = authViewModel::updateEmail,
                            onPasswordChange = authViewModel::updatePassword,
                            onLogin = { email, password ->
                                authViewModel.setLoading()
                                onLogin(email, password)
                            },
                            onSignUp = { authViewModel.setSignUp(true) },
                            onForgetPassword = {
                                authViewModel.setForgettingPassword(true)
                            },
                            isLoading = state.isLoading,
                            errorMessage = errorMessage
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { authViewModel.setForgettingPassword(true) }) {
                            Text("Forgot your password?")
                        }
                    }
                }
            }
        }
    }
}

