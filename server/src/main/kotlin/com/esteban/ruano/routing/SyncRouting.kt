package com.esteban.ruano.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.sync.SyncDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.SyncRepository

fun Route.syncRouting(
    syncRepository: SyncRepository
){
    route("/sync") {
        post {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val result = syncRepository.sync(
                userId,
                call.receive<SyncDTO>()
            )
            call.respond(result)
        }
    }
}