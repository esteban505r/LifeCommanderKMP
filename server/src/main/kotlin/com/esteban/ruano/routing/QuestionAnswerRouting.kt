package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.questions.CreateQuestionAnswerDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.QuestionAnswerRepository
import java.util.*

fun Route.questionAnswerRouting(questionAnswerRepository: QuestionAnswerRepository) {

    route("/question-answers") {
        get("/byDailyJournal/{dailyJournalId}") {
            val dailyJournalId = UUID.fromString(call.parameters["dailyJournalId"]!!)
            call.respond(questionAnswerRepository.getByDailyJournalId(dailyJournalId))
        }

        get("/byQuestion/{questionId}") {
            val questionId = UUID.fromString(call.parameters["questionId"]!!)
            call.respond(questionAnswerRepository.getByQuestionId(questionId))
        }

        post("/{dailyJournalId}") {
            val dailyJournalId = UUID.fromString(call.parameters["dailyJournalId"]!!)
            val answer = call.receive<CreateQuestionAnswerDTO>()
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val wasCreated = questionAnswerRepository.create(userId, dailyJournalId, answer)
            if (wasCreated != null) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        delete("/{id}") {
            val id = UUID.fromString(call.parameters["id"]!!)
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val wasDeleted = questionAnswerRepository.delete(userId, id)
            if (wasDeleted) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
} 