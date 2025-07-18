package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.dailyjournal.CreateDailyJournalDTO
import com.esteban.ruano.models.dailyjournal.UpdateDailyJournalDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.DailyJournalRepository
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.Validator
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.dailyJournalRouting(dailyJournalRepository: DailyJournalRepository) {

    route("/daily-journals") {
        get {
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            call.respond(dailyJournalRepository.getAll(userId, limit, offset))
        }

        get("/byDateRange") {
            val startDate = call.request.queryParameters["startDate"]
            val endDate = call.request.queryParameters["endDate"]
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if (startDate == null || endDate == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing start date or end date")
                return@get
            }

            try {
                val journals = dailyJournalRepository.getAllByDateRange(userId, startDate, endDate, limit, offset)
                call.respond(journals)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid date")
            }
        }

        get("/entry/{date}") {
            val dateString = call.parameters["date"]
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if (dateString == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing date parameter")
                return@get
            }

            try {
                val date = dateString.toLocalDate()
                val entry = dailyJournalRepository.getJournalEntryByDate(userId, date)
                if (entry != null) {
                    call.respond(entry)
                } else {
                    call.respond(HttpStatusCode.NotFound, "No journal entry found for this date")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid date format")
            }
        }

        get("/entries") {
            val startDate = call.request.queryParameters["startDate"]
            val endDate = call.request.queryParameters["endDate"]
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if (startDate == null || endDate == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing start date or end date")
                return@get
            }

            try {
                val start = startDate.toLocalDate()
                val end = endDate.toLocalDate()
                val entries = dailyJournalRepository.getJournalEntriesInRange(userId, start, end)
                call.respond(entries)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid date format")
            }
        }

        post {
            val journal = call.receive<CreateDailyJournalDTO>()
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if (!Validator.isValidDateFormat(journal.date)) {
                call.respond(HttpStatusCode.BadRequest, "Invalid date format")
                return@post
            }

            try {
                val wasCreated = dailyJournalRepository.create(userId, journal)
                if (wasCreated != null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error creating daily journal: ${e.message}")
            }
        }

        route("/{id}") {
            get {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val journal = dailyJournalRepository.getByIdAndUserId(id, userId)
                if (journal != null) {
                    call.respond(journal)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val journal = call.receive<UpdateDailyJournalDTO>()
                val wasUpdated = dailyJournalRepository.update(userId, id, journal)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            delete {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val wasDeleted = dailyJournalRepository.delete(userId, id)
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
} 