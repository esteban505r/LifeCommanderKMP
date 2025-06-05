package com.esteban.ruano.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.repository.BlogRepository
import com.esteban.ruano.utils.Validator
import java.io.File


fun Route.blogRouting(
    blogRepository: BlogRepository
) {
    route("/blog") {
        get("/posts/{slug}") {
            val slug = call.parameters["slug"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val post = blogRepository.getPostBySlug(slug)
            if (post.isEmpty()) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respondText(post, ContentType.Text.Plain)
        }

        post("/posts") {
            val multipart = call.receiveMultipart()
            var title: String? = null
            var description: String? = null
            var tags: List<String> = emptyList()
            var imageUrl: String? = null
            var slug: String? = null
            var publishedDate: String? = null
            var category = "Uncategorized"
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

            if (title == null || slug == null || publishedDate == null || file == null) {
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
            )
            call.respond(HttpStatusCode.Created, mapOf("id" to postId.toString(), "slug" to slug))

        }

        get("/posts") {
            val filter = call.request.queryParameters["filter"] ?: ""
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val date = call.request.queryParameters["date"]

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
                        date = date
                    )
                    call.respond(posts)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
            } else {
                call.respond(
                    blogRepository.getPosts(
                        limit = limit,
                        offset = offset,
                        pattern = filter
                    )
                )
            }
        }
    }

}