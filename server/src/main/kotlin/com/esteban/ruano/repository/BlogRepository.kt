package com.esteban.ruano.repository

import com.esteban.ruano.models.tasks.PostDTO
import com.esteban.ruano.service.BlogService
import com.esteban.ruano.utils.parseDate
import java.io.File
import java.util.UUID

class BlogRepository(private val blogService: BlogService) {

    fun getPostFromS3(slug: String, password: String? = null): String {
        return blogService.getPostFromS3(slug, password)
    }

    fun getPostBySlug(slug: String, password: String? = null): PostDTO? {
        return blogService.getPostBySlug(slug, password)
    }

    fun verifyPostPassword(slug: String, password: String): Boolean {
        return blogService.verifyPostPassword(slug, password)
    }

    fun createPost(
        title: String,
        slug: String,
        imageUrl: String,
        description: String,
        category: String,
        tags: List<String>,
        content: File,
        s3Key: String,
        publishedDate: String,
        password: String? = null
    ): UUID {
        return blogService.createPost(
            title = title,
            slug = slug,
            imageUrl = imageUrl,
            description = description,
            categoryName = category,
            tags = tags,
            content = content,
            s3Key = s3Key,
            publishedDate = publishedDate,
            password = password
        )
    }

    fun getPosts(
        date: String? = null,
        limit: Int = 10,
        offset: Long = 0,
        pattern: String = "",
        category: String? = null,
        password: String? = null,
        includeProtected: Boolean = false
    ): List<PostDTO> {
        return blogService.getPosts(
            pattern = pattern,
            limit = limit,
            offset = offset,
            category = category,
            date = date?.let { parseDate(it) },
            password = password,
            includeProtected = includeProtected
        )
    }
}