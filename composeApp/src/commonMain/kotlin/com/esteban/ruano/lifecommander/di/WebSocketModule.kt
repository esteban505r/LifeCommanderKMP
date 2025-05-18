package com.esteban.ruano.lifecommander.di

import com.esteban.ruano.lifecommander.service.TimerService
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import org.koin.dsl.module

val webSocketModule = module {
    single {
        HttpClient {
            install(WebSockets)
        }
    }

    single { (baseUrl: String, authToken: String) ->
        TimerService(
            httpClient = get(),
            baseUrl = baseUrl,
            authToken = authToken
        )
    }
} 