package services.tags

import com.lifecommander.models.*
import encodeUrlWithSpaces
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.esteban.ruano.lifecommander.utils.TAGS_ENDPOINT
import com.esteban.ruano.lifecommander.utils.appHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class TagService(
    private val client: HttpClient
) {
    suspend fun getAllTags(token: String): List<Tag> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get(TAGS_ENDPOINT) {
                    appHeaders(token)
                }.body<List<Tag>>()
                response
            } catch (e: Exception) {
                throw TagServiceException("Failed to fetch tags: ${e.message}", e)
            }
        }
    }

    suspend fun getTagById(token: String, tagId: String): Tag {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get("$TAGS_ENDPOINT/$tagId") {
                    appHeaders(token)
                }.body<Tag>()
                response
            } catch (e: Exception) {
                throw TagServiceException("Failed to fetch tag: ${e.message}", e)
            }
        }
    }

    suspend fun createTag(token: String, createTagRequest: CreateTagRequest): Tag {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post(TAGS_ENDPOINT) {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(createTagRequest)
                }
                if (response.status == HttpStatusCode.Created) {
                    response.body<Tag>()
                } else {
                    throw TagServiceException("Failed to create tag: ${response.status}")
                }
            } catch (e: Exception) {
                throw TagServiceException("Failed to create tag: ${e.message}", e)
            }
        }
    }

    suspend fun updateTag(token: String, tagId: String, updateTagRequest: UpdateTagRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.patch("$TAGS_ENDPOINT/$tagId") {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(updateTagRequest)
                }
                response.status == HttpStatusCode.OK
            } catch (e: Exception) {
                throw TagServiceException("Failed to update tag: ${e.message}", e)
            }
        }
    }

    suspend fun deleteTag(token: String, tagId: String) {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.delete("$TAGS_ENDPOINT/$tagId") {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TagServiceException("Failed to delete tag: ${response.status}")
                }
            } catch (e: Exception) {
                throw TagServiceException("Failed to delete tag: ${e.message}", e)
            }
        }
    }

    suspend fun updateTaskTags(token: String, taskId: String, tagIds: List<String>) {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.put("${com.esteban.ruano.lifecommander.utils.TASKS_ENDPOINT}/$taskId/tags") {
                    appHeaders(token)
                    contentType(ContentType.Application.Json)
                    setBody(UpdateTaskTagsRequest(tagIds))
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TagServiceException("Failed to update task tags: ${response.status}")
                }
            } catch (e: Exception) {
                throw TagServiceException("Failed to update task tags: ${e.message}", e)
            }
        }
    }

    suspend fun attachTagToTask(token: String, taskId: String, tagId: String) {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post("${com.esteban.ruano.lifecommander.utils.TASKS_ENDPOINT}/$taskId/tags/$tagId") {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TagServiceException("Failed to attach tag: ${response.status}")
                }
            } catch (e: Exception) {
                throw TagServiceException("Failed to attach tag: ${e.message}", e)
            }
        }
    }

    suspend fun detachTagFromTask(token: String, taskId: String, tagId: String) {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.delete("${com.esteban.ruano.lifecommander.utils.TASKS_ENDPOINT}/$taskId/tags/$tagId") {
                    appHeaders(token)
                }
                if (response.status != HttpStatusCode.OK) {
                    throw TagServiceException("Failed to detach tag: ${response.status}")
                }
            } catch (e: Exception) {
                throw TagServiceException("Failed to detach tag: ${e.message}", e)
            }
        }
    }
}

class TagServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)

