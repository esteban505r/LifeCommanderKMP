package com.esteban.ruano.repository

import com.esteban.ruano.models.tasks.PostDTO
import com.esteban.ruano.service.BlogService
import com.esteban.ruano.utils.parseDate
import java.io.File
import java.util.UUID

class BlogRepository(private val blogService: BlogService) {

    fun getPostBySlug(slug: String): String {
        val post = blogService.getPostFromS3(slug)
        return post
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
        publishedDate: String
    ): UUID {
        return blogService.createPost(
            title = title,
            slug = slug,
            imageUrl = imageUrl,
            description = description,
            category = category,
            tags = tags,
            content = content,
            s3Key = s3Key,
            publishedDate = publishedDate
        )
    }

    fun getPosts(
        date: String? = null,
        limit: Int = 10,
        offset: Long = 0,
        pattern: String = "",
    ): List<PostDTO> {
        return blogService.getPosts(
            limit = limit,
            offset = offset,
            pattern = pattern,
            date = date?.let{parseDate(it)}
        )
    }

}