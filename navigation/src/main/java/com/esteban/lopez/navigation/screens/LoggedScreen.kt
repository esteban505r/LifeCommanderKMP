package com.esteban.lopez.navigation.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.lifecommander.utils.BottomNavIconUtils
import com.esteban.ruano.navigation.NavHostWrapper
import com.esteban.ruano.timers_presentation.service.TimerServiceManagerViewModel
import com.esteban.ruano.timers_presentation.ui.screens.viewmodel.TimerViewModel
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.timer.TimerPlaybackStatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LoggedScreen(onRootNavigate: (String) -> Unit) {
    val navController = rememberNavController()
    val bottomNavColor = Color.White
    val timerServiceManagerViewModel: TimerServiceManagerViewModel = hiltViewModel()
    val timerViewModel: TimerViewModel = hiltViewModel()
    val timerState by timerViewModel.viewState.collectAsState()

    // Initialize timer service when user is logged in
    LaunchedEffect(Unit) {
        timerServiceManagerViewModel.timerServiceManager.initialize()
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = bottomNavColor,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()

                val currentRoute = navBackStackEntry?.destination?.route


                Routes.BASE.getAllRoutes().forEach { screen ->
                    Log.d("CURRENT ROUTE" , currentRoute.toString())
                    Log.d("SCREEN NAME" , screen.name.toString())
                    val selected = when {
                        // Handle TO_DO grouping
                        currentRoute?.startsWith(Routes.BASE.TO_DO.name) ?: false &&
                                screen.name.startsWith(Routes.BASE.TO_DO.name) -> true
                        // Handle HEALTH grouping (workout and nutrition)
                        (currentRoute?.startsWith(Routes.BASE.WORKOUT.name) ?: false ||
                         currentRoute?.startsWith(Routes.BASE.NUTRITION.name) ?: false) &&
                                screen.name.startsWith(Routes.BASE.HEALTH.name) -> true
                        // Handle OTHERS grouping (finance, timers, journal, study)
                        (currentRoute?.startsWith(Routes.BASE.FINANCE.name) ?: false ||
                         currentRoute?.startsWith(Routes.BASE.TIMERS.name) ?: false ||
                         currentRoute?.startsWith(Routes.BASE.JOURNAL.name) ?: false ||
                         currentRoute?.startsWith(Routes.BASE.STUDY.name) ?: false) &&
                                screen.name.startsWith(Routes.BASE.OTHERS.name) -> true
                        // Exact match
                        currentRoute == screen.name -> true
                        else -> false
                    }
                    BottomNavigationItem(
                        unselectedContentColor = Gray,
                        selectedContentColor = Color.Black,
                        modifier = Modifier.height(75.dp),
                        icon = {
                            Image(
                                painter = org.jetbrains.compose.resources.painterResource(
                                    BottomNavIconUtils.getResourceIconByString(
                                        screen.name,
                                        selected
                                    )
                                ),
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .size(if (selected) 56.dp else 24.dp),
                                contentDescription = screen.name
                            )
                        },
                        label = if (selected) {
                            null
                        } else {
                            { Text(text = screen.label ?: "") }
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.name) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        val playback = timerState.timerPlaybackState

        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Global active timer bar (visible on any screen when a timer is running)
            if (playback?.currentTimer != null && playback.status == TimerPlaybackStatus.Running) {
                ActiveTimerBar(
                    playbackState = playback,
                    onPause = { timerViewModel.performAction(com.esteban.ruano.timers_presentation.ui.intent.TimerIntent.PauseTimer) },
                    onStop = { timerViewModel.performAction(com.esteban.ruano.timers_presentation.ui.intent.TimerIntent.StopTimer) }
                )
            }

            // Main navigation content
            NavHostWrapper(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                navController = navController,
                shouldShowOnboarding = false,
            )
        }
        // Alternative approaches you can try:

        // 1. Manual approach with WindowInsets:
        // NavHostWrapper(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .background(bottomNavColor)
        //         .padding(WindowInsets.systemBars.asPaddingValues())
        //         .padding(it),
        //     navController = navController,
        //     shouldShowOnboarding = false,
        // )

        // 2. Separate status and navigation bar handling:
        // NavHostWrapper(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .background(bottomNavColor)
        //         .padding(WindowInsets.statusBars.asPaddingValues())
        //         .padding(WindowInsets.navigationBars.asPaddingValues())
        //         .padding(it),
        //     navController = navController,
        //     shouldShowOnboarding = false,
        // )

        // 3. Using utility functions:
        // NavHostWrapper(
        //     modifier = SystemBarUtils.withSystemBarBackground(
        //         backgroundColor = bottomNavColor,
        //         modifier = Modifier.padding(it)
        //     ),
        //     navController = navController,
        //     shouldShowOnboarding = false,
        // )
    }
}

@Composable
private fun ActiveTimerBar(
    playbackState: TimerPlaybackState,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colors.primary.copy(alpha = 0.08f),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Active Timer",
                    tint = MaterialTheme.colors.primary
                )
                Text(
                    text = playbackState.currentTimer?.name ?: "",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = (playbackState.remainingMillis / 1000).toString() + "s",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onPause) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Timer",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
                IconButton(onClick = onStop) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Timer",
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}
