package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.repository.*
import com.esteban.ruano.lifecommander.models.ErrorResponse
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.gatherScheduledTransactionFilters
import com.esteban.ruano.utils.gatherTransactionFilters
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
    categoryKeywordRepository: CategoryKeywordRepository,
    scheduledTransactionRepository: ScheduledTransactionRepository,
) {
    route("/finance") {
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
                val updated = accountRepository.update(userId, id, dto.name, dto.initialBalance,dto.type, dto.currency)
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
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 100
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                val scheduledBaseDate = call.parameters["scheduledBaseDate"]?.toLocalDate()
                val filters = call.gatherTransactionFilters()
                call.respond(transactionRepository.getAll(userId, limit, offset, filters,scheduledBaseDate))
            }
            get("/byAccount/{accountId}") {
                val accountId = UUID.fromString(call.parameters["accountId"]!!)
                call.respond(transactionRepository.getByAccount(accountId))
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
            get("/withProgress") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                val searchPattern = call.parameters["search"]
                val categories = call.parameters.getAll("category")
                val referenceDate = call.parameters["referenceDate"]?.toLocalDate()
                val minAmount = call.parameters["minAmount"]?.toDoubleOrNull()
                val maxAmount = call.parameters["maxAmount"]?.toDoubleOrNull()

                if(referenceDate == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing referenceDate parameter"))
                    return@get
                }

                val filters = BudgetFilters(
                    searchPattern = searchPattern,
                    categories = categories,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                )
                call.respond(budgetRepository.getAllProgress(userId, limit,offset, filters,referenceDate))
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
                call.respond(budgetRepository.getProgress(userId, id))
            }
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateBudgetDTO>()
                val id = budgetRepository.create(
                    userId,
                    dto.name,
                    dto.amount,
                    dto.category,
                    dto.startDate.toLocalDate(),
                    dto.endDate?.toLocalDate(),
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
                    dto.startDate?.toLocalDate(),
                    dto.endDate?.toLocalDate()
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

            get("unbudgeted/transactions") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                val filters = call.gatherTransactionFilters()
                val referenceDate = call.parameters["referenceDate"]?.toLocalDate()
                call.respond(budgetRepository.getUnbudgetedTransactions(userId,referenceDate!!, filters))
            }

            post("unbudgeted/categorize") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val referenceDate = call.parameters["referenceDate"]?.toLocalDate()
                    ?: Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                
                val categorizedCount = budgetRepository.categorizeUnbudgetedTransactions(userId, referenceDate)
                call.respond(mapOf("categorizedCount" to categorizedCount))
            }

            post("transactions/categorize") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val referenceDate = call.parameters["referenceDate"]?.toLocalDate()
                    ?: Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                
                val categorizedCount = budgetRepository.categorizeAllTransactions(userId, referenceDate)
                call.respond(mapOf("categorizedCount" to categorizedCount))
            }

            get("/{id}/transactions") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val referenceDate = call.parameters["referenceDate"]?.toLocalDate()
                    ?: Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                val filters = call.gatherTransactionFilters()
                call.respond(budgetRepository.getBudgetTransactions(userId, id, referenceDate, filters))
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
                call.respond(savingsGoalRepository.getProgress(userId, id))
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
                    dto.targetDate.toLocalDate()
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
                    dto.targetDate?.toLocalDate()
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

        // Category Keywords routes
        route("/category-keywords") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(categoryKeywordRepository.getKeywordsByUser(userId))
            }

            get("/by-category/{category}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val category = try {
                    Category.valueOf(call.parameters["category"]!!)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid category"))
                    return@get
                }
                call.respond(categoryKeywordRepository.getKeywordsByCategory(userId, category))
            }

            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateCategoryKeywordDTO>()
                val id = categoryKeywordRepository.addKeyword(
                    userId,
                    dto.category,
                    dto.keyword
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
                val dto = call.receive<UpdateCategoryKeywordDTO>()
                val updated = categoryKeywordRepository.updateKeyword(
                    userId,
                    id,
                    dto.keyword
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
                val deleted = categoryKeywordRepository.deleteKeyword(userId, id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            delete("/by-category/{category}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val category = try {
                    Category.valueOf(call.parameters["category"]!!)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid category"))
                    return@delete
                }
                val deleted = categoryKeywordRepository.deleteAllKeywordsForCategory(userId, category)
                if (deleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

        route("/scheduled-transactions") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                val filters = call.gatherScheduledTransactionFilters()
                call.respond(scheduledTransactionRepository.getAll(userId, limit, offset, filters))
            }
            get("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val scheduledTransaction = scheduledTransactionRepository.getById(id, userId)
                if (scheduledTransaction == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(scheduledTransaction)
            }
            get("/byAccount/{accountId}") {
                val accountId = UUID.fromString(call.parameters["accountId"]!!)
                call.respond(scheduledTransactionRepository.getByAccount(accountId))
            }
            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateScheduledTransactionDTO>()
                val id = scheduledTransactionRepository.create(
                    userId = userId,
                    description = dto.description,
                    amount = dto.amount,
                    startDate = dto.startDate,
                    frequency = dto.frequency,
                    interval = dto.interval,
                    type = dto.type,
                    category = dto.category,
                    accountId = UUID.fromString(dto.accountId),
                    applyAutomatically = dto.applyAutomatically
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
                val dto = call.receive<UpdateScheduledTransactionDTO>()
                val updated = scheduledTransactionRepository.update(
                    id = id,
                    userId = userId,
                    description = dto.description,
                    amount = dto.amount,
                    startDate = dto.startDate,
                    frequency = dto.frequency,
                    interval = dto.interval,
                    type = dto.type,
                    category = dto.category,
                    applyAutomatically = dto.applyAutomatically
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
                val deleted = scheduledTransactionRepository.delete( userId,id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

