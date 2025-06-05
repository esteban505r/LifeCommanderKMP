package com.esteban.ruano.routing

import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.PortfolioRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import lopez.esteban.com.models.portfolio.CreatePortfolioDTO
import lopez.esteban.com.models.portfolio.UpdatePortfolioDTO
import java.util.UUID


fun Route.portfolioRouting(portfolioRepository: PortfolioRepository) {

    authenticate {
        route("/portfolio") {
            get {
                val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
                val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
                val category = call.request.queryParameters["category"]
                val featured = call.request.queryParameters["featured"]?.toBoolean()
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id

                val portfolios = portfolioRepository.getByUserId(userId, limit, offset, category, featured)
                call.respond(portfolios)
            }

            post {
                val portfolio = call.receive<CreatePortfolioDTO>()
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val portfolioId = portfolioRepository.create(userId, portfolio)

                if (portfolioId != null) {
                    call.respond(HttpStatusCode.Created, mapOf("id" to portfolioId.toString()))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            route("/{id}") {
                // Get specific portfolio by ID
                get {
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val portfolio = portfolioRepository.getByIdAndUserId(id, userId)

                    if (portfolio != null) {
                        call.respond(portfolio)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                // Update portfolio
                patch {
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val portfolio = call.receive<UpdatePortfolioDTO>()
                    val wasUpdated = portfolioRepository.update(userId, id, portfolio)

                    if (wasUpdated) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                // Delete portfolio
                delete {
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val wasDeleted = portfolioRepository.delete(userId, id)

                    if (wasDeleted) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }

    // Public endpoint for viewing portfolios (no authentication required)
    route("/public/portfolio") {
        get {
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val category = call.request.queryParameters["category"]
            val featured = call.request.queryParameters["featured"]?.toBoolean()

            val portfolios = portfolioRepository.getAllPublic(limit, offset, category, featured)
            call.respond(portfolios)
        }
    }
}