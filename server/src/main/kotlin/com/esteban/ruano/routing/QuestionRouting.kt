package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.questions.CreateQuestionDTO
import com.esteban.ruano.models.questions.UpdateQuestionDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.QuestionRepository
import java.util.*

fun Route.questionRouting(questionRepository: QuestionRepository) {

    route("/questions") {
        get {
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            call.respond(questionRepository.getAll(userId, limit, offset))
        }

        post {
            val question = call.receive<CreateQuestionDTO>()
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val wasCreated = questionRepository.create(userId, question)
            if (wasCreated != null) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        route("/{id}") {
            get {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val question = questionRepository.getByIdAndUserId(id, userId)
                if (question != null) {
                    call.respond(question)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val question = call.receive<UpdateQuestionDTO>()
                val wasUpdated = questionRepository.update(userId, id, question)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            delete {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val wasDeleted = questionRepository.delete(userId, id)
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
} 