package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.tasks.CreateTaskDTO
import com.esteban.ruano.models.tasks.UpdateTaskDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.TaskRepository
import com.esteban.ruano.utils.Validator
import java.util.*

fun Route.tasksRouting(taskRepository: TaskRepository) {

    route("/tasks") {
        get {
            val filter = call.request.queryParameters["filter"]?:""
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val date = call.request.queryParameters["date"]
            val withOverdue = call.request.queryParameters["withOverdue"]?.toBoolean()?:true
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if(date!=null){
                try{
                    call.respond(taskRepository.getAll(userId,filter,limit,offset,date,withOverdue))
                }
                catch(e: Exception){
                    call.respond(HttpStatusCode.BadRequest,"Invalid date")
                    return@get
                }
            }

            call.respond(taskRepository.getAll(userId,filter,limit,offset))
        }

        get("/byDateRange"){
            val startDate = call.request.queryParameters["startDate"]
            val endDate = call.request.queryParameters["endDate"]
            val filter = call.request.queryParameters["filter"]?:""
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if(startDate==null || endDate==null){
                call.respond(HttpStatusCode.BadRequest,"Missing start date or end date")
                return@get
            }

            try{
                val tasks = taskRepository.getAllByDate(userId,startDate,endDate,filter,limit,offset)
                call.respond(tasks)
            }
            catch(e: Exception){
                call.respond(HttpStatusCode.BadRequest,"Invalid date")
            }

        }

        get("/byDateRangeWithSmartFiltering"){
            val startDate = call.request.queryParameters["startDate"]
            val endDate = call.request.queryParameters["endDate"]
            val filter = call.request.queryParameters["filter"]?:""
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val isTodayFilter = call.request.queryParameters["isTodayFilter"]?.toBoolean() ?: false
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id

            if(startDate==null || endDate==null){
                call.respond(HttpStatusCode.BadRequest,"Missing start date or end date")
                return@get
            }

            try{
                val tasks = taskRepository.getAllByDateWithSmartFiltering(
                    userId, startDate, endDate, filter, limit, offset, isTodayFilter
                )
                call.respond(tasks)
            }
            catch(e: Exception){
                call.respond(HttpStatusCode.BadRequest,"Invalid date")
            }
        }

        get("/noDueDate"){
            val filter = call.request.queryParameters["filter"]?:""
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val tasks = taskRepository.getAllNoDueDate(userId,filter,limit,offset)
            call.respond(tasks)
        }

        post {
            val task = call.receive<CreateTaskDTO>()
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val wasCreated = taskRepository.create(userId,task)
            if (wasCreated!=null) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }

        }

        route("/{id}/complete") {
            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dateTime = call.request.queryParameters["datetime"]
                val id = UUID.fromString(call.parameters["id"]!!)

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

                val wasCompleted = taskRepository.completeTask(userId,id,dateTime)
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
                val id = UUID.fromString(call.parameters["id"]!!)
                val wasCompleted = taskRepository.unCompleteTask(userId,id)
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
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val task = taskRepository.getByIdAndUserId(id,userId)
                if (task != null) {
                    call.respond(task)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val id = UUID.fromString(call.parameters["id"]!!)
                val task = call.receive<UpdateTaskDTO>()
                val updateTaskDTO = task
                val wasUpdated = taskRepository.update(userId,id,updateTaskDTO)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            delete {
                val id = UUID.fromString(call.parameters["id"]!!)
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val wasDeleted = taskRepository.delete(userId,id)
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}