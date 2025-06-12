package com.esteban.ruano.routing

import com.esteban.ruano.lifecommander.models.ErrorResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.repository.BlogRepository
import com.esteban.ruano.service.PostCategoryService
import com.esteban.ruano.models.blog.CreatePostCategoryDTO
import com.esteban.ruano.models.blog.UpdatePostCategoryDTO
import com.esteban.ruano.models.blog.PasswordVerificationRequest
import com.esteban.ruano.models.blog.PasswordVerificationResponse
import com.esteban.ruano.utils.Validator
import com.esteban.ruano.utils.X_CATEGORY_PASSWORD_HEADER
import com.esteban.ruano.utils.X_POST_PASSWORD_HEADER
import io.ktor.server.plugins.BadRequestException
import java.io.File
import java.util.UUID


fun Route.blogRouting(
    blogRepository: BlogRepository,
    postCategoryService: PostCategoryService
) {
    route("/blog") {
        get("/posts/{slug}") {
            val slug = call.parameters["slug"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val password = call.request.headers[X_POST_PASSWORD_HEADER]
                ?: call.request.queryParameters["password"]

            println("Fetching post with slug: $slug, password: $password")

            try {
                val post = blogRepository.getPostFromS3(slug, password)
                call.respondText(post, ContentType.Text.Plain)
            } catch (e: Exception) {
                when (e) {
                    is io.ktor.server.plugins.NotFoundException -> {
                        println("Post not found: $slug")
                        call.respond(HttpStatusCode.NotFound)
                    }
                    is io.ktor.server.plugins.BadRequestException -> {
                        println("Bad request for post: $slug")
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("Password required", code = 403)
                        )
                    }
                    else -> {
                        println("Error retrieving post: ${e.message}")
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                }
            }
        }

        post("/posts/{slug}/verify-password") {
            val slug = call.parameters["slug"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<PasswordVerificationRequest>()
            
            val isValid = blogRepository.verifyPostPassword(slug, request.password)
            call.respond(
                PasswordVerificationResponse(
                    success = isValid,
                    message = if (isValid) "Password correct" else "Invalid password"
                )
            )
        }

        get("/posts/{slug}/info") {
            val slug = call.parameters["slug"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val password = call.request.headers["X-Post-Password"] 
                ?: call.request.queryParameters["password"]

            val post = blogRepository.getPostBySlug(slug, password)
            if (post != null) {
                call.respond(post)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/posts") {
            val multipart = call.receiveMultipart()
            var title: String? = null
            var description: String? = null
            var tags: List<String> = emptyList()
            var imageUrl: String? = null
            var slug: String? = null
            var publishedDate: String? = null
            var category:String? = null
            var password: String? = null
            var file: File? = null
            var s3key: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "title" -> title = part.value
                            "slug" -> {
                                slug = part.value
                                s3key = "${part.value}.md"
                            }
                            "category" -> {
                                if (part.value.isBlank()) {
                                    call.respond(HttpStatusCode.BadRequest, "Category cannot be empty")
                                    return@forEachPart
                                }
                                category = part.value
                            }
                            "password" -> password = if (part.value.isBlank()) null else part.value
                            "description" -> description = part.value
                            "tags" -> tags = part.value.split(",").map { it.trim() }
                            "imageUrl" -> imageUrl = part.value
                            "publishedDate" -> publishedDate = part.value
                        }
                    }

                    is PartData.FileItem -> {
                        if (part.name == "file") {
                            val tempFile = File.createTempFile("upload-", ".tmp")
                            part.streamProvider()
                                .use { its -> tempFile.outputStream().buffered().use { its.copyTo(it) } }
                            file = tempFile
                        }
                    }

                    else -> {}
                }
                part.dispose()
            }

            if (title == null || slug == null || publishedDate == null || file == null || category == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                return@post
            }

            val postId = blogRepository.createPost(
                title!!,
                slug!!,
                imageUrl ?: "",
                description ?: "",
                category,
                tags,
                file!!,
                s3key!!,
                publishedDate!!,
                password
            )
            call.respond(HttpStatusCode.Created, mapOf("id" to postId.toString(), "slug" to slug))
        }

        get("/posts") {
            val filter = call.request.queryParameters["filter"] ?: ""
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val date = call.request.queryParameters["date"]
            val password = call.request.headers[X_POST_PASSWORD_HEADER] ?:
            call.request.headers[X_CATEGORY_PASSWORD_HEADER] ?: call.request.queryParameters["password"]
            val category = call.request.queryParameters["category"]
            val includeProtected = call.request.queryParameters["includeProtected"]?.toBoolean() ?: false

            if (date != null) {
                val isValid = Validator.isValidDateFormat(date)
                if (!isValid) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid date format")
                    return@get
                }
                try {
                    val posts = blogRepository.getPosts(
                        limit = limit,
                        offset = offset,
                        pattern = filter,
                        category = category,
                        date = date,
                        password = password,
                        includeProtected = includeProtected
                    )
                    call.respond(posts)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            } else {
                try {
                    val posts = blogRepository.getPosts(
                        limit = limit,
                        offset = offset,
                        pattern = filter,
                        category = category,
                        password = password,
                        includeProtected = includeProtected
                    )
                    call.respond(posts)
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e is BadRequestException) {
                        call.respond(HttpStatusCode.Forbidden, "Invalid password")
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
                    }
                    return@get
                }
            }
        }

        route("/categories") {
            get {
                val activeOnly = call.request.queryParameters["active"]?.toBoolean() ?: true
                call.respond(postCategoryService.getAllCategories(activeOnly))
            }

            get("/{id}") {
                val id = UUID.fromString(call.parameters["id"]!!)
                val category = postCategoryService.getCategoryById(id)
                if (category != null) {
                    call.respond(category)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            get("/slug/{slug}") {
                val slug = call.parameters["slug"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val category = postCategoryService.getCategoryBySlug(slug)
                if (category != null) {
                    call.respond(category)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            // Verify category password
            post("/{id}/verify-password") {
                val id = UUID.fromString(call.parameters["id"]!!)
                val request = call.receive<PasswordVerificationRequest>()
                
                val isValid = postCategoryService.verifyCategoryPassword(id, request.password)
                call.respond(
                    PasswordVerificationResponse(
                        success = isValid,
                        message = if (isValid) "Password correct" else "Invalid password"
                    )
                )
            }

            // Verify category password by slug
            post("/slug/{slug}/verify-password") {
                val slug = call.parameters["slug"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<PasswordVerificationRequest>()
                
                val isValid = postCategoryService.verifyCategoryPasswordBySlug(slug, request.password)
                call.respond(
                    PasswordVerificationResponse(
                        success = isValid,
                        message = if (isValid) "Password correct" else "Invalid password"
                    )
                )
            }

            post {
                val categoryDTO = call.receive<CreatePostCategoryDTO>()
                try {
                    val categoryId = postCategoryService.createCategory(categoryDTO)
                    call.respond(HttpStatusCode.Created, mapOf("id" to categoryId.toString()))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Failed to create category")))
                }
            }

            patch("/{id}") {
                val id = UUID.fromString(call.parameters["id"]!!)
                val updateDTO = call.receive<UpdatePostCategoryDTO>()
                val wasUpdated = postCategoryService.updateCategory(id, updateDTO)
                if (wasUpdated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}") {
                val id = UUID.fromString(call.parameters["id"]!!)
                val hardDelete = call.request.queryParameters["hard"]?.toBoolean() ?: false
                
                val wasDeleted = if (hardDelete) {
                    postCategoryService.hardDeleteCategory(id)
                } else {
                    postCategoryService.deleteCategory(id)
                }
                
                if (wasDeleted) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}