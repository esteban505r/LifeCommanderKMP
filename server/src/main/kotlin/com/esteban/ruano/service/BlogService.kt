package com.esteban.ruano.service

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.plugins.*
import kotlinx.datetime.LocalDate
import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.Post
import com.esteban.ruano.database.entities.Posts
import com.esteban.ruano.models.tasks.PostDTO
import com.esteban.ruano.utils.parseDateTime
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.transactions.transaction
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.util.UUID


class BlogService : BaseService() {

    val dotenv: Dotenv = dotenv {
        ignoreIfMissing = true
    }

    val awsAccessKey = dotenv["AWS_ACCESS_KEY_ID"]
    val awsSecretKey = dotenv["AWS_SECRET_ACCESS_KEY"]
    val awsRegion = dotenv["AWS_REGION"] ?: "us-east-1"

    val credentials = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
    )

    val bucketName = dotenv["S3_BUCKET_NAME"] ?: "estebanruanoposts"

    val s3Client = S3Client.builder()
        .credentialsProvider(credentials)
        .region(Region.US_EAST_1)
        .build()

    fun getPostFromS3(slug: String): String {
        try {
            val key = "$slug.md"
            val s3Object = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            )

            val content = s3Object.readAllBytes().toString(Charsets.UTF_8)
            return content

        } catch (e: NoSuchKeyException) {
            throw NotFoundException("Post not found")
        }

    }

    fun createPost(
        title: String,
        slug: String,
        imageUrl: String,
        description: String,
        tags: List<String>,
        category: String,
        content: File,
        s3Key: String,
        publishedDate: String
    ): UUID {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build()
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(content))

        return transaction {
            val item = Posts.insert {
                it[this.title] = title
                it[this.slug] = slug
                it[this.imageUrl] = imageUrl
                it[this.description] = description
                it[this.tags] = tags
                it[this.category] = category
                it[this.s3Key] = s3Key
                it[this.publishedDate] = parseDateTime(publishedDate)
            }.resultedValues?.firstOrNull()?.getOrNull(Posts.id)
            UUID.fromString(item.toString())
        } ?: throw BadRequestException("Failed to create post")
    }

    fun getPosts(
        pattern: String, limit: Int, offset: Long, date: LocalDate? = null
    ): List<PostDTO> {
        return transaction {
            Post.find {
                (Posts.slug like "%${pattern.lowercase()}%")
                    .and(date?.let { Posts.publishedDate.date() eq it } ?: Op.TRUE)
            }.orderBy(Posts.publishedDate to SortOrder.DESC)
                .limit(limit, offset).toList().map { it.toDTO() }
        }
    }

}