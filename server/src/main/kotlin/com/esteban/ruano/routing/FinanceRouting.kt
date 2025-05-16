package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.repository.*
import com.esteban.ruano.database.converters.*
import com.esteban.ruano.lifecommander.models.ErrorResponse
import com.esteban.ruano.service.TransactionService
import com.lifecommander.finance.model.ImportTransactionsRequest
import com.lifecommander.finance.model.ImportTransactionsResponse
import com.lifecommander.finance.model.TransactionImportPreviewRequest
import kotlinx.datetime.*
import java.util.*

fun Route.financeRouting(
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    savingsGoalRepository: SavingsGoalRepository,
) {
    route("/finance") {
        // Account routes
        route("/accounts") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(accountRepository.getAll(userId))
            }
            get("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val account = accountRepository.getById(userId, id)
                if (account != null) {
                    call.respond(account)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateAccountDTO>()
                val id = accountRepository.create(
                    userId,
                    dto.name,
                    dto.type,
                    dto.balance,
                    dto.currency
                )
                if (id != null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            patch("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val dto = call.receive<UpdateAccountDTO>()
                val updated = accountRepository.update(userId, id, dto.name, dto.type, dto.currency)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            delete("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val deleted = accountRepository.delete(userId, id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            get("/total-balance") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(TotalBalanceResponseDTO(accountRepository.getTotalBalance(userId)))
            }
        }

        // Transaction routes
        route("/transactions") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(transactionRepository.getAll(userId))
            }
            get("/byAccount/{accountId}") {
                val accountId = UUID.fromString(call.parameters["accountId"]!!)
                call.respond(transactionRepository.getByAccount(accountId))
            }
            get("/byDateRange") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<DateRangeQueryDTO>()
                val startDate = dto.startDate
                val endDate = dto.endDate
                call.respond(transactionRepository.getByDateRange(userId, startDate, endDate))
            }
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateTransactionDTO>()
                val id = transactionRepository.create(
                    userId,
                    dto.amount,
                    dto.description,
                    dto.date,
                    dto.type,
                    dto.category,
                    UUID.fromString(dto.accountId)
                )
                if (id != null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            patch("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val dto = call.receive<UpdateTransactionDTO>()
                val updated = transactionRepository.update(
                    userId,
                    id,
                    dto.amount,
                    dto.description,
                    dto.date,
                    dto.type,
                    dto.category
                )
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            delete("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val deleted = transactionRepository.delete(userId, id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            post("/import") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<ImportTransactionsRequest>()
                try {
                    val transactionIds = transactionRepository.importTransactionsFromText(
                        userId = userId,
                        text = dto.text,
                        accountId = dto.accountId
                    )
                    call.respond(ImportTransactionsResponse(transactionIds.map { it.toString() }))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Invalid input format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to import transactions"))
                }
            }
            post("/import/preview"){
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<TransactionImportPreviewRequest>()
                try {
                    val preview = transactionRepository.importPreview(
                        userId = userId,
                        text = dto.text,
                        accountId = dto.accountId
                    )
                    call.respond(preview)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Invalid input format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to import transactions"))
                }
            }
        }

        // Budget routes
        route("/budgets") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(budgetRepository.getAll(userId))
            }
            get("/byDateRange") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<DateRangeQueryDTO>()
                val startDate = LocalDate.parse(dto.startDate)
                val endDate = LocalDate.parse(dto.endDate)
                call.respond(budgetRepository.getByDateRange(userId, startDate, endDate))
            }
            get("/{id}/progress") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                call.respond(ProgressResponseDTO(budgetRepository.getProgress(userId, id)))
            }
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateBudgetDTO>()
                val id = budgetRepository.create(
                    userId,
                    dto.name,
                    dto.amount,
                    dto.category,
                    dto.startDate,
                    dto.endDate
                )
                if (id != null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            patch("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val dto = call.receive<UpdateBudgetDTO>()
                val updated = budgetRepository.update(
                    userId,
                    id,
                    dto.name,
                    dto.amount,
                    dto.category,
                    dto.startDate,
                    dto.endDate
                )
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            delete("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val deleted = budgetRepository.delete(userId, id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

        // Savings goal routes
        route("/savings-goals") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(savingsGoalRepository.getAll(userId))
            }
            get("/{id}/progress") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                call.respond(ProgressResponseDTO(savingsGoalRepository.getProgress(userId, id)))
            }
            get("/{id}/remaining") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                call.respond(RemainingAmountResponseDTO(savingsGoalRepository.getRemainingAmount(userId, id)))
            }
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateSavingsGoalDTO>()
                val id = savingsGoalRepository.create(
                    userId,
                    dto.name,
                    dto.targetAmount,
                    dto.targetDate
                )
                if (id != null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            patch("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val dto = call.receive<UpdateSavingsGoalDTO>()
                val updated = savingsGoalRepository.update(
                    userId,
                    id,
                    dto.name,
                    dto.targetAmount,
                    dto.targetDate
                )
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            delete("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val deleted = savingsGoalRepository.delete(userId, id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            post("/{id}/progress") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val dto = call.receive<UpdateSavingsGoalProgressDTO>()
                val updated = savingsGoalRepository.updateProgress(userId, id, dto.amount)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

