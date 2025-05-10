package ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import services.auth.AuthService
import ui.ui.viewmodels.AuthViewModel
import ui.screens.AuthScreen
import ui.screens.HomeScreen
import ui.state.AuthState
import ui.viewmodels.AppViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authService: AuthService = koinInject(),
    authViewModel: AuthViewModel = koinViewModel(),
    appViewModel: AppViewModel,
    startDestination: String = Screen.Auth.route,
) {
    val authState by authViewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route == Screen.Auth.route) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != Screen.Auth.route) {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthenticated = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onLogin = { email, password ->
                    authViewModel.updateEmail(email)
                    authViewModel.updatePassword(password)
                    authViewModel.login()
                },
                onSignUp = { email, password ->
                    authViewModel.updateEmail(email)
                    authViewModel.updatePassword(password)
                    authViewModel.signUp()
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            HomeScreen(
                onTaskClick = {},
                onHabitClick = {},
                appViewModel = appViewModel,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
            )
        }
    }
} 