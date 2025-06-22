package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.nutrition.CreateRecipeDTO
import com.esteban.ruano.models.nutrition.CreateRecipeTrackDTO
import com.esteban.ruano.models.nutrition.UpdateRecipeDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.NutritionRepository
import com.esteban.ruano.utils.gatherRecipeFilters
import java.util.*

fun Route.nutritionRouting(nutritionRepository: NutritionRepository) {

    route("/nutrition") {
        route("/recipes") {
            get {
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val filters = call.gatherRecipeFilters()
                
                call.respond(nutritionRepository.getRecipesWithFilters(userId, limit, offset, filters))
            }

            get("/all") {
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val filters = call.gatherRecipeFilters()
                
                call.respond(nutritionRepository.getRecipesWithFilters(userId, limit, offset, filters))
            }

            get("/{id}") {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val result = nutritionRepository.getRecipe(userId, id)
                if (result != null) {
                    call.respond(result)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            get("/byDay/{day}") {
                val day = call.parameters["day"]!!.toInt()
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val filters = call.gatherRecipeFilters()
                
                call.respond(nutritionRepository.getRecipesByDayWithFilters(userId, day, limit, offset, filters))
            }

            post {
                val task = call.receive<CreateRecipeDTO>()
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val wasCreated = nutritionRepository.create(userId, task)
                if (wasCreated != null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            patch("/{id}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val recipeDTO = call.receive<UpdateRecipeDTO>()
                val wasUpdated = nutritionRepository.update(userId,id,recipeDTO)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            delete("/{id}") {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val wasDeleted = nutritionRepository.delete(userId, id)
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
        
        route("/dashboard") {
            get {
                val date = call.request.queryParameters["date"] ?: ""
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id

                call.respond(nutritionRepository.getDashboard(userId, date))
            }
        }

        // Recipe Tracking Endpoints
        route("/tracking") {
            post("/consume") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val trackRequest = call.receive<CreateRecipeTrackDTO>()
                val trackId = nutritionRepository.trackRecipeConsumption(userId, trackRequest)
                if (trackId != null) {
                    call.respond(HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/range") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val startDate = call.request.queryParameters["startDate"]!!
                val endDate = call.request.queryParameters["endDate"]!!
                call.respond(nutritionRepository.getRecipeTracksByDateRange(userId, startDate, endDate))
            }

            get("/recipe/{recipeId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val recipeId = call.parameters["recipeId"]!!
                call.respond(nutritionRepository.getRecipeTracksByRecipe(userId, recipeId))
            }

            delete("/track/{trackId}") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val trackId = call.parameters["trackId"]!!
                val success = nutritionRepository.deleteRecipeTrack(userId, trackId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}