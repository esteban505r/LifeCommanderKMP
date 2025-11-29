package com.esteban.ruano.core_data.di

import android.content.Context
import com.esteban.ruano.core.di.HabitsAlarmReceiverClass
import com.esteban.ruano.core.di.TasksAlarmReceiverClass
import com.esteban.ruano.core.di.TasksNotificationHelper
import com.esteban.ruano.core.di.WebSocketHttpClient
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NotificationsHelper
import com.esteban.ruano.core.models.habits.HabitNotificationWrapper
import com.esteban.ruano.core.models.tasks.TasksNotificationWrapper
import com.esteban.ruano.core_data.di.interceptor.AuthInterceptor
import com.esteban.ruano.core_data.helpers.AlarmHelper
import com.esteban.ruano.core_data.workManager.factories.CustomWorkerFactory
import com.esteban.ruano.core_data.workManager.habits.factories.CheckHabitsWorkerFactory
import com.esteban.ruano.core_data.workManager.tasks.factories.CheckTasksWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.HttpHeaders
import io.ktor.http.encodedPath
import io.ktor.serialization.gson.gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideKtorHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            install(ContentEncoding) {
                gzip()
                deflate()
                identity()
            }
        }
    }

    @Provides
    @Singleton
    @WebSocketHttpClient
    fun provideWebSocketHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            install(WebSockets)
        }.also { client ->
            // Add request logging interceptor after client creation
            client.requestPipeline.intercept(HttpRequestPipeline.State) {
                val request = context
                android.util.Log.d("SocketClient", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                android.util.Log.d("SocketClient", "🔌 [HTTP Request] Request Details:")
                android.util.Log.d("SocketClient", "  Method: ${request.method}")
                android.util.Log.d("SocketClient", "  URL: ${request.url}")
                android.util.Log.d("SocketClient", "  Protocol: ${request.url.protocol}")
                android.util.Log.d("SocketClient", "  Host: ${request.url.host}")
                android.util.Log.d("SocketClient", "  Port: ${request.url.port}")
                android.util.Log.d("SocketClient", "  Path: ${request.url.encodedPath}")
                android.util.Log.d("SocketClient", "  Full URL: ${request.url.buildString()}")
                android.util.Log.d("SocketClient", "  Headers:")
                request.headers.entries().forEach { header ->
                    val headerName = header.key
                    val headerValue = header.value.firstOrNull() ?: ""
                    if (headerName == HttpHeaders.Authorization) {
                        // Mask authorization token for security
                        val maskedValue = if (headerValue.length > 20) {
                            "${headerValue.take(20)}...${headerValue.takeLast(10)}"
                        } else {
                            "***masked***"
                        }
                        android.util.Log.d("SocketClient", "    $headerName: $maskedValue")
                    } else {
                        android.util.Log.d("SocketClient", "    $headerName: ${header.value.joinToString(", ")}")
                    }
                }
                android.util.Log.d("SocketClient", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            }
        }
    }



    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context
    ): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideCustomWorkerFactory(
        checkHabitsWorkerFactory: CheckHabitsWorkerFactory,
        habitNotificationWrapper: HabitNotificationWrapper,
        alarmHelper: AlarmHelper,
        tasksNotificationWrapper: TasksNotificationWrapper,
        checkTasksWorkerFactory: CheckTasksWorkerFactory,
        @HabitsAlarmReceiverClass habitsAlarmReceiverClass: Class<*>,
        @TasksAlarmReceiverClass tasksAlarmReceiverClass: Class<*>,
        @TasksNotificationHelper tasksNotificationHelper: NotificationsHelper,
        preferences: Preferences
    ): CustomWorkerFactory {
        return CustomWorkerFactory(
            checkHabitsWorkerFactory = checkHabitsWorkerFactory,
            habitNotificationWrapper = habitNotificationWrapper,
            alarmHelper = alarmHelper,
            tasksNotificationWrapper = tasksNotificationWrapper,
            checkTasksWorkerFactory = checkTasksWorkerFactory,
            tasksNotificationsHelper = tasksNotificationHelper,
            habitsAlarmReceiverClass = habitsAlarmReceiverClass,
            tasksAlarmReceiverClass = tasksAlarmReceiverClass,
            preferences = preferences
        )
    }
}