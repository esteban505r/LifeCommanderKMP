package com.esteban.ruano.plugins

import com.esteban.ruano.lifecommander.timer.TimerWebSocketClientMessage
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
            serializersModule = SerializersModule {
                polymorphic(TimerWebSocketClientMessage::class) {

                }
            }
        })

    }
}
