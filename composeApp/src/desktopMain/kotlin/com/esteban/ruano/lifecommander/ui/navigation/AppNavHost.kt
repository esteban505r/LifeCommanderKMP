package ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.esteban.ruano.lifecommander.ui.navigation.CategoryKeywordMapperDestination
import com.esteban.ruano.lifecommander.ui.components.AppLayout
import com.esteban.ruano.lifecommander.ui.navigation.CalendarScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.HabitsScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.MealsScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.PomodorosScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.SettingsScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.TasksScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.TimerListDetailDestination
import com.esteban.ruano.lifecommander.ui.navigation.TimersScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.WorkoutScreenDestination
import com.esteban.ruano.lifecommander.ui.navigation.routes.BudgetTransactionsRoute
import com.esteban.ruano.lifecommander.ui.navigation.routes.TimerListDetailRoute
import com.esteban.ruano.lifecommander.ui.screens.BudgetTransactionsScreen
import com.esteban.ruano.lifecommander.ui.screens.FinancialScreenDestination
import com.esteban.ruano.lifecommander.ui.screens.FinanceImporterPlaceholderScreen
import com.esteban.ruano.lifecommander.ui.screens.BudgetTransactionsPlaceholderScreen
import com.esteban.ruano.lifecommander.ui.screens.CategoryKeywordMapperPlaceholderScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import services.auth.AuthService
import ui.ui.viewmodels.AuthViewModel
import ui.screens.AuthScreen
import com.esteban.ruano.lifecommander.ui.screens.HomeScreen
import com.esteban.ruano.lifecommander.ui.screens.TransactionImportScreen
import com.esteban.ruano.lifecommander.ui.screens.StatisticsScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import ui.state.AuthState
import ui.viewmodels.AppViewModel
import ui.viewmodels.DailyJournalViewModel
import services.NightBlockService
import ui.viewmodels.HabitsViewModel
import ui.viewmodels.TasksViewModel
import com.esteban.ruano.lifecommander.ui.navigation.JournalScreenDestination
import com.esteban.ruano.lifecommander.ui.screens.StatisticsScreenDestination
import com.esteban.ruano.lifecommander.ui.viewmodels.SettingsViewModel

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Finance : Screen("finance")
    object FinanceImporter : Screen("finance_importer")
    object Calendar : Screen("calendar")
    object Timers : Screen("timers")
    object Settings : Screen("settings")
    object CategoryKeywordMapper : Screen("category_keyword_mapper")
    object Pomodoros : Screen("pomodoros")
    object Tasks : Screen("tasks")
    object Habits : Screen("habits")
    object Meals : Screen("meals")
    object Workout : Screen("workout")
    object Statistics : Screen("statistics")
    object Journal : Screen("journal")
    object Test : Screen("test")
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    taskViewModel: TasksViewModel = koinViewModel(),
    habitViewModel: HabitsViewModel = koinViewModel(),
    authService: AuthService = koinInject(),
    financeViewModel: FinanceViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    appViewModel: AppViewModel = koinViewModel(),
    dailyJournalViewModel: DailyJournalViewModel = koinViewModel(),
    nightBlockService: NightBlockService = koinInject(),
    timersViewModel: TimersViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    startDestination: String = Screen.Auth.route,
) {
    val authState by authViewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                timersViewModel.connectWebSocket()
                timersViewModel.loadPomodoros()
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
            onTestNotification = {
                settingsViewModel.testNotification()
            },
            onTestDueTasksNotification = {
                settingsViewModel.testDueTasksNotification()
            },
            onTestDueHabitsNotification = {
                settingsViewModel.testDueHabitsNotification()
            }
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
                        onNavigateToTasks = {
                            navController.navigate(Screen.Tasks.route)
                        },
                        onNavigateToHabits = {
                            navController.navigate(Screen.Habits.route)
                        },
                        onNavigateToStatistics = {
                            navController.navigate(Screen.Statistics.route)
                        },
                        onTaskClick = {

                        },
                        onHabitClick = {

                        },
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
                                budgetId = budgetId
                            ))
                        },
                        onOpenCategoryKeywordMapper = {
                            navController.navigate(Screen.CategoryKeywordMapper.route)
                        }
                    )
                }

                composable(Screen.FinanceImporter.route) {
                    TransactionImportScreen(
                        modifier = modifier,
                        onImportComplete = {
                            navController.navigateUp()
                        }
                    )
                }

                composable<BudgetTransactionsRoute>(
                ) { backStackEntry ->
                    val args = backStackEntry.toRoute<BudgetTransactionsRoute>()
                    BudgetTransactionsScreen(
                        budgetId = args.budgetId,
                        onBack = {
                            navController.navigateUp()
                        },
                        modifier = modifier,
                        financeActions = financeViewModel,
                        state = financeViewModel.state.collectAsState().value,
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
                        timersViewModel = timersViewModel,
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

                composable(Screen.CategoryKeywordMapper.route) {
                    CategoryKeywordMapperPlaceholderScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        modifier = modifier
                    )
                }

                composable(Screen.Pomodoros.route) {
                    PomodorosScreenDestination(
                        modifier = modifier,
                        timersViewModel = timersViewModel,
                        onBack = {
                            navController.navigateUp()
                        }
                    )
                }

                composable(Screen.Tasks.route) {
                    TasksScreenDestination(
                        tasksViewModel = taskViewModel,
                        onTaskClick = { taskId ->

                        })
                }

                composable(Screen.Habits.route) {
                    HabitsScreenDestination(
                        habitsViewModel = habitViewModel,
                        onHabitClick = { habitId ->
                        }
                    )
                }

                composable(Screen.Meals.route) {
                    MealsScreenDestination(
                        onNavigateUp = { navController.popBackStack() },
                        onNewRecipe = { /* TODO: Navigate to NewEditRecipe screen */ },
                        onDetailRecipe = { recipeId -> /* TODO: Navigate to RecipeDetail screen with recipeId */ }
                    )
                }

                composable(Screen.Workout.route) {
                    WorkoutScreenDestination()
                }

                composable(Screen.Journal.route) {
                    JournalScreenDestination()
                }

                composable(Screen.Statistics.route) {
                    StatisticsScreenDestination(
                        modifier = modifier,
                        onNavigateBack = {
                            navController.navigateUp()
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