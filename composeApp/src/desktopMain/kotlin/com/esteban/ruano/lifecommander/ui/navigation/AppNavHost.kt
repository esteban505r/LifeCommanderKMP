package ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.esteban.ruano.lifecommander.ui.components.AppLayout
import com.esteban.ruano.lifecommander.ui.navigation.CalendarScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.SettingsScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.TimerListDetailDestination
import com.esteban.ruano.lifecommander.ui.navigation.TimersScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.routes.BudgetTransactionsRoute
import com.esteban.ruano.lifecommander.ui.navigation.routes.TimerListDetailRoute
import com.esteban.ruano.lifecommander.ui.screens.BudgetTransactionsScreen
import com.esteban.ruano.lifecommander.ui.screens.FinancialScreenDestination
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import services.auth.AuthService
import ui.ui.viewmodels.AuthViewModel
import ui.screens.AuthScreen
import com.esteban.ruano.lifecommander.ui.screens.HomeScreen
import com.esteban.ruano.lifecommander.ui.screens.TransactionImportScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import ui.state.AuthState
import ui.viewmodels.AppViewModel
import ui.viewmodels.DailyJournalViewModel
import services.NightBlockService
import ui.viewmodels.HabitsViewModel
import ui.viewmodels.TasksViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Finance : Screen("finance")
    object FinanceImporter : Screen("finance_importer")
    object Calendar : Screen("calendar")
    object Timers : Screen("timers")
    object Settings : Screen("settings")
    object BudgetTransactions : Screen("budget_transactions/{budgetId}") {
        fun createRoute(budgetId: String) = "budget_transactions/$budgetId"
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    taskViewModel: TasksViewModel = koinViewModel(),
    habitViewModel: HabitsViewModel = koinViewModel(),
    authService: AuthService = koinInject(),
    authViewModel: AuthViewModel = koinViewModel(),
    appViewModel: AppViewModel = koinViewModel(),
    dailyJournalViewModel: DailyJournalViewModel = koinViewModel(),
    nightBlockService: NightBlockService = koinInject(),
    timersViewModel: TimersViewModel = koinViewModel(),
    startDestination: String = Screen.Auth.route,
) {
    val authState by authViewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                timersViewModel.connectWebSocket()
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

    if (authState is AuthState.Authenticated) {
        AppLayout(
            appViewModel = appViewModel,
            dailyJournalViewModel = dailyJournalViewModel,
            nightBlockService = nightBlockService,
            navController = navController,
            onLogoutClick = {
                authViewModel.logout()
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }
            },
            taskViewModel = taskViewModel,
            habitViewModel = habitViewModel,
            timersViewModel = timersViewModel,
        ) {
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
                        onSignUp = {name, email, password ->
                            authViewModel.updateName(name)
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
                        tasksViewModel = taskViewModel,
                        habitsViewModel = habitViewModel,
                    )
                }

                composable(Screen.Finance.route) {
                    FinancialScreenDestination(
                        modifier = modifier,
                        financialViewModel = koinViewModel(),
                        onOpenImporter = {
                            navController.navigate(Screen.FinanceImporter.route)
                        },
                        onOpenBudgetTransactions = { budgetId ->
                            navController.navigate(BudgetTransactionsRoute(
                                budgetId
                            ))
                        }
                    )
                }

                composable(Screen.FinanceImporter.route) {
                    TransactionImportScreen(
                        onImportComplete = {
                            navController.navigateUp()
                        },
                    )
                }

                composable<BudgetTransactionsRoute>(
                ) { backStackEntry ->
                    val args = backStackEntry.toRoute<BudgetTransactionsRoute>()
                    BudgetTransactionsScreen(
                        budgetId = args.budgetId,
                        onBack = {
                            navController.navigateUp()
                        }
                    )
                }

                composable(Screen.Timers.route) {
                    TimersScreenDestination(
                        modifier = modifier,
                        timersViewModel = timersViewModel,
                        onNavigateToDetail = { timerListId ->
                            navController.navigate(TimerListDetailRoute(timerListId))
                        }
                    )
                }

                composable<TimerListDetailRoute> { backStackEntry: NavBackStackEntry ->
                    val arguments = backStackEntry.toRoute<TimerListDetailRoute>()
                    TimerListDetailDestination (
                        modifier = modifier,
                        timerListId = arguments.timerId,
                        onBack = {
                            navController.navigateUp()
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreenDestination(
                        modifier = modifier,
                        timersViewModel = timersViewModel,
                        onNavigateToTimers = {
                            navController.navigate(Screen.Timers.route)
                        }
                    )
                }

                composable(Screen.Calendar.route) {
                    CalendarScreenDestination(
                        modifier = modifier,
                        onTaskClick = { id ->
                            // TODO: Navigate to task details
                        },
                        onHabitClick = { id ->
                            // TODO: Navigate to habit details
                        }
                    )
                }
            }
        }
    } else {
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
                    onSignUp = {name, email, password ->
                        authViewModel.updateName(name)
                        authViewModel.updateEmail(email)
                        authViewModel.updatePassword(password)
                        authViewModel.signUp()
                    }
                )
            }
        }
    }
} 