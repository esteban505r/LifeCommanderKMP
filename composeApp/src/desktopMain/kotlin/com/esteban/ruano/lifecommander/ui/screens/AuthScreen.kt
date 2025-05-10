package ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import ui.state.AuthState
import ui.ui.viewmodels.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onAuthenticated: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (email: String, password: String) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val isSignUp by authViewModel.isSignUp.collectAsState()
    
    LaunchedEffect(authState) {
        println("AuthState: $authState")
        when (authState) {
            is AuthState.Authenticated -> onAuthenticated()
            else -> {}
        }
    }

    if(authState is AuthState.Unauthenticated) {
        val state = authState as AuthState.Unauthenticated
        val errorMessage = state.errorMessage
        if (isSignUp) {
            SignUpScreen(
                onSignUp = { email, password ->
                    authViewModel.setLoading()
                    onSignUp(email, password)
                },
                onNavigateToLogin = {
                    authViewModel.setSignUp(false)
                    authViewModel.logout()
                },
                isLoading = state.isLoading,
                errorMessage = errorMessage
            )
        } else {
            LoginScreen(
                email = state.email,
                password =  state.password,
                onEmailChange = { authViewModel.updateEmail(it) },
                onPasswordChange = { authViewModel.updatePassword(it) },
                onLogin = { email, password ->
                    authViewModel.setLoading()
                    onLogin(email, password)
                },
                isLoading = state.isLoading,
                errorMessage = errorMessage
            )
        }
    }
} 