package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.models.tags.CreateTagDTO
import com.esteban.ruano.models.tags.UpdateTagDTO
import com.esteban.ruano.models.users.LoggedUserDTO
import com.esteban.ruano.repository.TagRepository
import java.util.*

fun Route.tagsRouting(tagRepository: TagRepository) {
    route("/tags") {
        get {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val tags = tagRepository.listTags(userId)
            call.respond(tags)
        }

        post {
            val userId = call.authentication.principal<LoggedUserDTO>()!!.id
            val createTagDTO = call.receive<CreateTagDTO>()
            
            // Validate name
            if (createTagDTO.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Tag name cannot be empty")
                return@post
            }
            
            // Validate color format if provided
            if (createTagDTO.color != null && !createTagDTO.color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                call.respond(HttpStatusCode.BadRequest, "Invalid color format. Expected hex color (e.g., #FFAA00)")
                return@post
            }
            
            val tagId = tagRepository.createTag(userId, createTagDTO)
            if (tagId != null) {
                val tag = tagRepository.getTagById(userId, tagId)
                call.respond(HttpStatusCode.Created, tag!!)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Tag with this name already exists")
            }
        }

        route("/{id}") {
            get {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val tagId = UUID.fromString(call.parameters["id"]!!)
                val tag = tagRepository.getTagById(userId, tagId)
                if (tag != null) {
                    call.respond(tag)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            patch {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val tagId = UUID.fromString(call.parameters["id"]!!)
                val updateTagDTO = call.receive<UpdateTagDTO>()
                
                // Validate color format if provided
                if (updateTagDTO.color != null && !updateTagDTO.color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid color format. Expected hex color (e.g., #FFAA00)")
                    return@patch
                }
                
                // Validate name if provided
                if (updateTagDTO.name != null && updateTagDTO.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Tag name cannot be empty")
                    return@patch
                }
                
                val success = tagRepository.updateTag(userId, tagId, updateTagDTO)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Tag not found or slug conflict")
                }
            }

            delete {
                val userId = call.authentication.principal<LoggedUserDTO>()!!.id
                val tagId = UUID.fromString(call.parameters["id"]!!)
                val success = tagRepository.deleteTag(userId, tagId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

