package com.esteban.ruano.lifecommander

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.google.gson.Gson
import com.kdroid.composenotification.builder.AppConfig
import com.kdroid.composenotification.builder.ExperimentalNotificationsApi
import com.kdroid.composenotification.builder.Notification
import com.kdroid.composenotification.builder.NotificationInitializer
import com.kdroid.composetray.tray.api.Tray
import di.appModule
import extractResourceToTempFile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import models.TimerModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ui.navigation.AppNavHost
import ui.state.AppState
import ui.theme.LifeCommanderTheme
import ui.viewmodels.AppViewModel
import utils.BackgroundServiceManager
import utils.Timers
import utils.timersKey
import java.time.Duration
import kotlin.system.exitProcess

@Composable
@Preview
fun App(
    appViewModel: AppViewModel
) {
    LifeCommanderTheme {
        AppNavHost(appViewModel = appViewModel)
    }
}

fun main() = application {
    // Initialize notifications
    NotificationInitializer.configure(
        AppConfig(
            appName = "LifeCommander",
        )
    )

    KoinApplication(application = {
        modules(
            appModule
        )
    }) {
        KoinContext {
            AppWindow()
        }
    }
}

@OptIn(ExperimentalNotificationsApi::class)
@Composable
fun ApplicationScope.AppWindow() {

    val viewModelStore = remember { ViewModelStore() }
    val viewModelStoreOwner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = viewModelStore
        }
    }

    val dataStore: DataStore<Preferences> = koinInject()
    val backgroundServiceManager: BackgroundServiceManager = koinInject()


    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val appViewModel: AppViewModel = koinViewModel()

        val appState: AppState = appViewModel.appState.collectAsState().value

        LaunchedEffect(Unit) {
            Timers.replaceTimers(
                dataStore.data.first()[timersKey]?.let {
                    Gson().fromJson(it, Array<TimerModel>::class.java).toList()
                } ?: emptyList()
            )
        }

        LaunchedEffect(Unit) {
            backgroundServiceManager.startPeriodicTask(
                "nightBlockCheck",
                Duration.ofMinutes(1),
            ) {
                appViewModel.checkNightBlock()
            }
        }


        if (appState.isOpen) {
            if (appState.isDialogOpen) {
                DialogWindow(
                    title = "Time Up!",
                    onCloseRequest = { appViewModel.hideDialog() }
                ) {
                    Surface(modifier = Modifier.wrapContentSize()) {
                        Column(
                            modifier = Modifier.padding(20.dp).fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(appState.dialogTitle, style = MaterialTheme.typography.h5)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(appState.dialogMessage, style = MaterialTheme.typography.body1)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { appViewModel.hideDialog() }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }

            Tray(
                iconPath = extractResourceToTempFile("icon.png", ".png"),
                windowsIconPath = extractResourceToTempFile("icon.png", ".png"),
                tooltip = "LifeCommander",
                primaryAction = {
                    if (appState.isMinimized) {
                        appViewModel.restore()
                    }
                }
            ) {
                Item("Show notifications") {
                    Notification(
                        title = "Test Notification",
                        message = "This is a test notification from LifeCommander",
                        onActivated = { /* Handle notification click */ },
                        onDismissed = { /* Handle notification dismissal */ },
                        onFailed = { /* Handle notification failure */ }
                    )
                }

                Divider()

                if (appState.isMinimized) {
                    Item("Open LifeCommander") {
                        appViewModel.restore()
                    }
                }

                Divider()

                Item("Exit") {
                    exitProcess(0)
                }
            }

            if (!appState.isMinimized) {
                Window(
                    onCloseRequest = { appViewModel.minimize() },
                    title = "LifeCommander",
                    icon = MyAppIcon,
                    onKeyEvent = {
                        if (it.type == KeyEventType.KeyDown) {
                            when {
                                it.isCtrlPressed && it.key == Key.T -> {
                                    appViewModel.showTimersDialog()
                                    true
                                }

                                it.isCtrlPressed && it.key == Key.P -> {
                                    if (appState.timersPaused) {
                                        appViewModel.playTimer()
                                    } else {
                                        appViewModel.pauseTimer()
                                    }
                                    true
                                }

                                it.isCtrlPressed && it.key == Key.S -> {
                                    appViewModel.stopTimer()
                                    true
                                }

                                else -> false
                            }
                        } else {
                            false
                        }
                    }
                ) {

                    App(
                        appViewModel
                    )
                }
            }
        }

    }

}

object MyAppIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color.Green, Offset(size.width / 4, 0f), Size(size.width / 2f, size.height))
        drawOval(Color.Blue, Offset(0f, size.height / 4), Size(size.width, size.height / 2f))
        drawOval(Color.Red, Offset(size.width / 4, size.height / 4), Size(size.width / 2f, size.height / 2f))
    }
}
