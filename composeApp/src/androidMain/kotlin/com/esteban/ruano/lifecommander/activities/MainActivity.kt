package com.esteban.ruano.lifecommander.activities

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.esteban.ruano.core.data.preferences.dataStore
import com.esteban.ruano.core.di.TasksNotificationHelper
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.lopez.core.utils.AppConstants.EMPTY_STRING
import com.esteban.ruano.core.utils.PermissionManager
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.utils.LocalMainState
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.theme.lexendFontFamily
import com.esteban.ruano.core_ui.view_model.MainViewModel
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.lifecommander.R
import com.esteban.ruano.lifecommander.activities.interfaces.NotificationActivity
import com.esteban.ruano.lifecommander.navigation.MainDestination
import com.esteban.ruano.ui.LifeCommanderTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity(), NotificationActivity {

    @Inject
    @TasksNotificationHelper
    lateinit var notificationHelper: NotificationsHelper

    private val mainViewModel: MainViewModel by viewModels()


    override fun prepareNotifications(onNotificationsPrepared: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionManager(this).checkPermissions(
                Manifest.permission.POST_NOTIFICATIONS,
                onPermissionsGranted = { isGranted ->
                    if (isGranted) {
                        onNotificationsPrepared()
                    } else {
                        Toast.makeText(
                            this,
                            "Permissions are required to show notifications",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        } else {
            onNotificationsPrepared()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }

        //LogUtils.logToDownloadsFolder(this, "Logs prepared ${System.currentTimeMillis().toLocalDateTime().plusHours(5).parseDateTime()}")
        prepareNotifications {
            Log.d("MainActivity", "Notifications prepared")
        }

        val TAG = "FirebaseMessaging"
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                // Don't show toast for FCM failures as they're common and not user-facing
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log the token (but don't show toast in production)
            Log.d(TAG, "FCM TOKEN: $token")
            // Only show toast in debug builds
            if (BuildConfig.DEBUG) {
                Toast.makeText(baseContext, "FCM Token: ${token.take(20)}...", Toast.LENGTH_SHORT).show()
            }
        })

        setContent {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val authTokenFlow = context.dataStore.data.map {
                    it[Preferences.KEY_AUTH_TOKEN] ?: EMPTY_STRING
                }

                val initial: Pair<String?, String?> = null to null

                authTokenFlow.runningFold(initial) { lastPair, next ->
                    lastPair.run {
                        val (_, last) = this
                        last to next
                    }
                }
                    .collect {
                        if (it.first?.isNotEmpty() == true && it.second?.isEmpty() == true) {
                            mainViewModel.performAction(MainIntent.ShowSnackBar(
                                context.getString(R.string.session_expired_message),
                                SnackbarType.INFO
                            ))
                        }
                    }
            }
            CompositionLocalProvider(
                LocalMainState provides mainViewModel.viewState.collectAsState().value
            ) {
                CompositionLocalProvider(LocalMainIntent provides mainViewModel::performAction) {
                    LifeCommanderTheme(
                        fontFamily = lexendFontFamily
                    ) {
                        val snackbarHostState = remember { SnackbarHostState() }
                        MainDestination(
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}