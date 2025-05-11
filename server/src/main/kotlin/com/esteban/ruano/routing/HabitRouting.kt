package com.esteban.ruano.routing
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.database.models.Frequency
import com.esteban.ruano.models.habits.CreateHabitDTO
import com.esteban.ruano.models.habits.UpdateHabitDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.HabitRepository
import com.esteban.ruano.utils.Validator
import java.util.*

fun Route.habitsRouting(
    habitRepository: HabitRepository
){
    route("/habits") {
        get {
            val filter = call.request.queryParameters["filter"]?:""
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val date = call.request.queryParameters["date"]
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if(date!=null){
                val isValid = Validator.isValidDateFormat(date)
                if(!isValid){
                    call.respond(HttpStatusCode.BadRequest,"Invalid date format")
                    return@get
                }
                try{
                    val habits = habitRepository.getAll(
                        userId,
                        filter,
                        limit,
                        offset,
                        date
                    )
                    call.respond(habits)
                }
                catch(e: Exception){
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            }

            call.respond(habitRepository.getAll(userId,filter,limit,offset))
        }
        post {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val habit = call.receive<CreateHabitDTO>()

            if(habit.dateTime==null){
                call.respond(HttpStatusCode.BadRequest,"DateTime is required")
                return@post
            }
            else{
                val isValid = Validator.isValidDateTimeFormat(habit.dateTime)
                if(!isValid){
                    call.respond(HttpStatusCode.BadRequest,"Invalid datetime format")
                    return@post
                }
            }


            try{
                if(habit.frequency.lowercase()==Frequency.ONE_TIME.value){
                    call.respond(HttpStatusCode.BadRequest,"Habit frequency cannot be one time, use tasks for that")
                    return@post
                }
                Frequency.valueOf(habit.frequency.uppercase())
            }
            catch(e: IllegalArgumentException){
                call.respond(HttpStatusCode.BadRequest,"Invalid frequency")
                return@post
            }

            val wasCreated = habitRepository.create(userId,habit)
            if (wasCreated!=null) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }

        }

        route("/{id}/complete") {
            patch {
                val id = call.parameters["id"]!!
                val dateTime = call.request.queryParameters["datetime"]
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id

                if(dateTime==null){
                    call.respond(HttpStatusCode.BadRequest,"DateTime is required")
                    return@patch
                }
                else{
                    val isValid = Validator.isValidDateTimeFormat(dateTime)
                    if(!isValid){
                        call.respond(HttpStatusCode.BadRequest,"Invalid datetime format")
                        return@patch
                    }
                }

                val wasCompleted = habitRepository.completeTask(id,dateTime, userId)

                if (wasCompleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

        route("/{id}/uncomplete") {
            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = call.parameters["id"]!!
                val dateTime = call.request.queryParameters["datetime"]

                if(dateTime==null){
                    call.respond(HttpStatusCode.BadRequest,"Datetime is required")
                    return@patch
                }
                else{
                    val isValid = Validator.isValidDateTimeFormat(dateTime)
                    if(!isValid){
                        call.respond(HttpStatusCode.BadRequest,"Invalid datetime format")
                        return@patch
                    }
                }

                val wasCompleted = habitRepository.unCompleteTask(id,dateTime,userId)
                if (wasCompleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

        route("/{id}") {
            get {
                val id = UUID.fromString(call.parameters["id"]!!)
                val date = call.request.queryParameters["date"]
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id

                if(date==null){
                    call.respond(HttpStatusCode.BadRequest,"Date is required")
                    return@get
                }
                else{
                    val isValid = Validator.isValidDateFormat(date)
                    if(!isValid){
                        call.respond(HttpStatusCode.BadRequest,"Invalid date format")
                        return@get
                    }
                }

                val habit = habitRepository.getByIdAndUserId(id,userId,date,)
                if (habit != null) {
                    call.respond(habit)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val habit = call.receive<UpdateHabitDTO>()
                val wasUpdated = habitRepository.update(userId,id,habit)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            delete {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val wasDeleted = habitRepository.delete(userId,UUID.fromString(call.parameters["id"]!!.toString()))
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }


    }
}