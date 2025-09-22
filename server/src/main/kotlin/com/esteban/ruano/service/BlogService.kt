package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.converters.verifyPassword
import com.esteban.ruano.database.entities.Post
import com.esteban.ruano.database.entities.PostCategories
import com.esteban.ruano.database.entities.PostCategory
import com.esteban.ruano.database.entities.Posts
import com.esteban.ruano.models.tasks.PostDTO
import com.esteban.ruano.utils.SecurityUtils
import com.esteban.ruano.utils.parseDateTime
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.plugins.*
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.util.*


class BlogService : BaseService() {

    // Load .env if present (dev). In prod this file typically won't exist.
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    // ---- Credentials strategy ------------------------------------------------
    // If .env provides explicit keys, use them (dev). Otherwise, use
    // DefaultCredentialsProvider (EC2 instance role, ECS task role, env, profile, etc.)
    private val credentialsProvider = run {
        val envKey = dotenv["AWS_ACCESS_KEY_ID"]
        val envSecret = dotenv["AWS_SECRET_ACCESS_KEY"]

        if (!envKey.isNullOrBlank() && !envSecret.isNullOrBlank()) {
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(envKey, envSecret)
            )
        } else {
            DefaultCredentialsProvider.builder().build()
        }
    }

    // ---- Region resolution ---------------------------------------------------
    // Order: .env -> AWS default region chain (env, sys props, config files, IMDS) -> fallback
    private val resolvedRegion: Region = run {
        val fromDotenv = dotenv["AWS_REGION"]
        if (!fromDotenv.isNullOrBlank()) {
            Region.of(fromDotenv)
        } else {
            DefaultAwsRegionProviderChain.builder().build().region ?: Region.US_EAST_1
        }
    }

    // ---- Bucket --------------------------------------------------------------
    private val bucketName: String = run {
        // Prefer .env, then environment, else your default
        dotenv["S3_BUCKET_NAME"]
            ?: System.getenv("S3_BUCKET_NAME")
            ?: "estebanruanoposts"
    }

    // ---- S3 client (single instance) ----------------------------------------
    private val s3: S3Client by lazy {
        S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(resolvedRegion)
            .build()
    }

    // -------------------------------------------------------------------------

    private fun hashPassword(password: String): String =
        at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
            .hashToString(12, password.toCharArray())

    fun getPostFromS3(slug: String, password: String? = null): String {
        try {
            // Verify post exists & password
            val post = transaction {
                Post.find { Posts.slug eq slug }.firstOrNull()
            } ?: throw NotFoundException("Post not found")

            transaction {
                if (!post.verifyPassword(password)) {
                    throw BadRequestException("Invalid password for protected content")
                }
            }

            val key = "$slug.md"
            val s3Object = s3.getObject(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            )

            return s3Object.readAllBytes().toString(Charsets.UTF_8)
        } catch (e: NoSuchKeyException) {
            throw NotFoundException("Post content not found")
        }
    }

    private fun findOrCreateCategory(categoryName: String): PostCategory {
        return transaction {
            PostCategory.find { PostCategories.name eq categoryName }.firstOrNull()
                ?: PostCategory.new {
                    name = categoryName
                    slug = categoryName.lowercase()
                        .replace(" ", "-")
                        .replace(Regex("[^a-z0-9-]"), "")
                    description = null
                    color = null
                    icon = null
                    displayOrder = 0
                    password = null
                }
        }
    }

    fun createPost(
        title: String,
        slug: String,
        imageUrl: String,
        description: String,
        tags: List<String>,
        categoryName: String,
        content: File,
        s3Key: String,
        publishedDate: String,
        password: String? = null
    ): UUID {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build()
        s3.putObject(putObjectRequest, RequestBody.fromFile(content))

        return transaction {
            val category = findOrCreateCategory(categoryName)

            val idValue = Posts.insert {
                it[this.title] = title
                it[this.slug] = slug
                it[this.imageUrl] = imageUrl
                it[this.description] = description
                it[this.tags] = tags
                it[this.categoryId] = category.id
                it[this.password] = password?.let { hashPassword(it) }
                it[this.s3Key] = s3Key
                it[this.publishedDate] = parseDateTime(publishedDate)
            }.resultedValues?.firstOrNull()?.getOrNull(Posts.id)
                ?: throw BadRequestException("Failed to create post")

            UUID.fromString(idValue.toString())
        }
    }

    fun getPosts(
        pattern: String,
        limit: Int,
        offset: Long,
        category: String? = null,
        date: LocalDate? = null,
        password: String? = null,
        includeProtected: Boolean = false
    ): List<PostDTO> {
        return transaction {
            val slugCondition = Posts.slug like "%${pattern.lowercase()}%"
            val dateCondition = date?.let { Posts.publishedDate.date() eq it }
            val categoryCondition = category?.let { Posts.categoryId eq UUID.fromString(category) }

            category?.let {
                val result = PostCategory.find { PostCategories.id eq UUID.fromString(it) }.firstOrNull()

                if (result?.password?.isEmpty() == false && password.isNullOrEmpty()) {
                    throw BadRequestException("Category requires a password")
                }
                if (result?.password?.isNotEmpty() == true && password?.isNotEmpty() == true) {
                    if (!SecurityUtils.checkPassword(password, result.password!!)) {
                        throw BadRequestException("Invalid password for category")
                    }
                }
            }

            var finalCondition: Op<Boolean> = slugCondition
            dateCondition?.let { finalCondition = finalCondition.and(it) }
            categoryCondition?.let { finalCondition = finalCondition.and(it) }

            Post.find { finalCondition }
                .orderBy(Posts.publishedDate to SortOrder.DESC)
                .limit(limit)
                .offset(offset * limit)
                .toList()
                .map { post -> post.toDTO(password) }
                .filter { postDTO ->
                    if (!includeProtected && password == null) !postDTO.requiresPassword else true
                }
        }
    }

    fun getPostBySlug(slug: String, password: String? = null): PostDTO? =
        transaction {
            Post.find { Posts.slug eq slug }.firstOrNull()?.toDTO(password)
        }

    fun verifyPostPassword(slug: String, password: String): Boolean =
        transaction {
            Post.find { Posts.slug eq slug }.firstOrNull()?.verifyPassword(password) ?: false
        }

    fun verifyCategoryPassword(categoryId: UUID, password: String): Boolean =
        transaction {
            val category = PostCategory.findById(categoryId) ?: return@transaction false
            category.password?.let { hashed -> SecurityUtils.checkPassword(password, hashed) } ?: false
        }
}