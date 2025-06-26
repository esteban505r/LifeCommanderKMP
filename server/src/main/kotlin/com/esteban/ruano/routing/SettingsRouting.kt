package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.SettingsRepository
import com.esteban.ruano.lifecommander.models.UserSettings

fun Route.settingsRouting(settingsRepository: SettingsRepository) {
    route("/settings") {
        get {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val settings = settingsRepository.getUserSettings(userId)
            call.respond(settings)
        }

        put {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val settings = call.receive<UserSettings>()
            val updatedSettings = settingsRepository.updateUserSettings(userId, settings)
            call.respond(updatedSettings)
        }
    }
} 