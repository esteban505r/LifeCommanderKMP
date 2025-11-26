package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.database.entities.StudyItemStage
import com.esteban.ruano.database.entities.StudyMode
import com.esteban.ruano.models.study.*
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.StudyRepository
import com.esteban.ruano.service.StudyImageService
import com.esteban.ruano.utils.Validator
import io.ktor.server.request.*
import io.ktor.server.request.receiveMultipart
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import java.io.File
import java.util.*

fun Route.studyRouting(
    studyRepository: StudyRepository,
    studyImageService: StudyImageService
) {
    route("/study") {
        // StudyTopic endpoints
        route("/topics") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val isActive = call.request.queryParameters["isActive"]?.toBoolean()
                try {
                    val topics = studyRepository.getAllTopics(userId, isActive)
                    call.respond(topics)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to fetch topics: ${e.message}")
                }
            }

            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateStudyTopicDTO>()
                
                if (dto.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Name is required")
                    return@post
                }

                try {
                    val id = studyRepository.createTopic(userId, dto)
                    if (id != null) {
                        call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to create topic: ${e.message}")
                }
            }

            route("/{id}") {
                get {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    
                    try {
                        val topic = studyRepository.getTopicById(userId, id)
                        if (topic != null) {
                            call.respond(topic)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to fetch topic: ${e.message}")
                    }
                }

                patch {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val dto = call.receive<UpdateStudyTopicDTO>()
                    
                    try {
                        val wasUpdated = studyRepository.updateTopic(userId, id, dto)
                        if (wasUpdated) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to update topic: ${e.message}")
                    }
                }

                delete {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    
                    try {
                        val wasDeleted = studyRepository.deleteTopic(userId, id)
                        if (wasDeleted) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to delete topic: ${e.message}")
                    }
                }
            }
        }

        // StudyItem endpoints
        route("/items") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val topicId = call.request.queryParameters["topicId"]
                val stage = call.request.queryParameters["stage"]
                val search = call.request.queryParameters["search"]
                
                try {
                    val items = studyRepository.getAllItems(userId, topicId, stage, search)
                    call.respond(items)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to fetch items: ${e.message}")
                }
            }

            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateStudyItemDTO>()
                
                if (dto.title.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Title is required")
                    return@post
                }

                // Validate stage
                try {
                    if (dto.stage.isNotBlank()) {
                        StudyItemStage.valueOf(dto.stage)
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid stage: ${dto.stage}")
                    return@post
                }

                try {
                    val id = studyRepository.createItem(userId, dto)
                    if (id != null) {
                        call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to create item: ${e.message}")
                }
            }

            route("/{id}") {
                get {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    
                    try {
                        val item = studyRepository.getItemById(userId, id)
                        if (item != null) {
                            call.respond(item)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to fetch item: ${e.message}")
                    }
                }

                patch {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val dto = call.receive<UpdateStudyItemDTO>()
                    
                    // Validate stage if provided
                    try {
                        dto.stage?.let { StudyItemStage.valueOf(it) }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid stage: ${dto.stage}")
                        return@patch
                    }

                    try {
                        val wasUpdated = studyRepository.updateItem(userId, id, dto)
                        if (wasUpdated) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to update item: ${e.message}")
                    }
                }

                delete {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    
                    try {
                        val wasDeleted = studyRepository.deleteItem(userId, id)
                        if (wasDeleted) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to delete item: ${e.message}")
                    }
                }
            }
        }

        // StudySession endpoints
        route("/sessions") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val topicId = call.request.queryParameters["topicId"]
                val mode = call.request.queryParameters["mode"]
                val startDate = call.request.queryParameters["startDate"]
                val endDate = call.request.queryParameters["endDate"]
                
                if (startDate != null && endDate != null) {
                    val isStartDateValid = Validator.isValidDateFormat(startDate)
                    val isEndDateValid = Validator.isValidDateFormat(endDate)
                    if (!isStartDateValid || !isEndDateValid) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid date format")
                        return@get
                    }
                }
                
                try {
                    val sessions = studyRepository.getAllSessions(userId, topicId, mode, startDate, endDate)
                    call.respond(sessions)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to fetch sessions: ${e.message}")
                }
            }

            post {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val dto = call.receive<CreateStudySessionDTO>()
                
                // Validate mode
                try {
                    StudyMode.valueOf(dto.mode)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid mode: ${dto.mode}")
                    return@post
                }

                try {
                    val id = studyRepository.createSession(userId, dto)
                    if (id != null) {
                        call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to create session: ${e.message}")
                }
            }

            route("/{id}") {
                get {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    
                    try {
                        val session = studyRepository.getSessionById(userId, id)
                        if (session != null) {
                            call.respond(session)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to fetch session: ${e.message}")
                    }
                }

                patch {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val dto = call.receive<UpdateStudySessionDTO>()
                    
                    // Validate mode if provided
                    try {
                        dto.mode?.let { StudyMode.valueOf(it) }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid mode: ${dto.mode}")
                        return@patch
                    }

                    try {
                        val wasUpdated = studyRepository.updateSession(userId, id, dto)
                        if (wasUpdated) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to update session: ${e.message}")
                    }
                }

                delete {
                    val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                    val id = UUID.fromString(call.parameters["id"]!!)
                    
                    try {
                        val wasDeleted = studyRepository.deleteSession(userId, id)
                        if (wasDeleted) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.BadRequest, "Failed to delete session: ${e.message}")
                    }
                }

                route("/complete") {
                    post {
                        val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                        val id = UUID.fromString(call.parameters["id"]!!)
                        val actualEnd = call.request.queryParameters["actualEnd"]
                        val notes = call.request.queryParameters["notes"]
                        
                        if (actualEnd == null) {
                            call.respond(HttpStatusCode.BadRequest, "actualEnd is required")
                            return@post
                        }
                        
                        val isValid = Validator.isValidDateTimeFormat(actualEnd)
                        if (!isValid) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid datetime format")
                            return@post
                        }
                        
                        try {
                            val wasCompleted = studyRepository.completeSession(userId, id, actualEnd, notes)
                            if (wasCompleted) {
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.InternalServerError)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            call.respond(HttpStatusCode.BadRequest, "Failed to complete session: ${e.message}")
                        }
                    }
                }
            }
        }

        // Icon upload endpoint
        route("/icons") {
            post("/upload") {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val multipart = call.receiveMultipart()
                var file: File? = null
                var fileName: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "fileName" -> fileName = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "file") {
                                val tempFile = File.createTempFile("study-icon-", ".tmp")
                                part.streamProvider()
                                    .use { its -> tempFile.outputStream().buffered().use { its.copyTo(it) } }
                                file = tempFile
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (file == null) {
                    call.respond(HttpStatusCode.BadRequest, "No file provided")
                    return@post
                }

                try {
                    val iconUrl = studyImageService.uploadIcon(file!!, userId, fileName)
                    call.respond(HttpStatusCode.OK, mapOf("url" to iconUrl))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to upload icon: ${e.message}")
                } finally {
                    file?.delete()
                }
            }
        }

        // Disciplines endpoint
        route("/disciplines") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                try {
                    val disciplines = studyRepository.getAllDisciplines(userId)
                    call.respond(disciplines)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to fetch disciplines: ${e.message}")
                }
            }
        }

        // Stats endpoint
        route("/stats") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val startDate = call.request.queryParameters["startDate"]
                val endDate = call.request.queryParameters["endDate"]
                
                if (startDate != null && endDate != null) {
                    val isStartDateValid = Validator.isValidDateFormat(startDate)
                    val isEndDateValid = Validator.isValidDateFormat(endDate)
                    if (!isStartDateValid || !isEndDateValid) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid date format")
                        return@get
                    }
                }
                
                try {
                    val stats = studyRepository.getStats(userId, startDate, endDate)
                    call.respond(stats)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Failed to fetch stats: ${e.message}")
                }
            }
        }
    }
}

