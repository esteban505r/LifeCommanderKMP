package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.nutrition.CreateRecipeDTO
import com.esteban.ruano.models.nutrition.UpdateRecipeDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.NutritionRepository
import java.util.*

fun Route.nutritionRouting(nutritionRepository: NutritionRepository) {

    route("/nutrition") {
        route("/recipes") {
            get {
                val filter = call.request.queryParameters["filter"] ?: ""
                val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
                val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id

                call.respond(nutritionRepository.getAllRecipes(userId, filter, limit, offset))
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
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                call.respond(nutritionRepository.getRecipesByDay(userId, day))
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
    }
}