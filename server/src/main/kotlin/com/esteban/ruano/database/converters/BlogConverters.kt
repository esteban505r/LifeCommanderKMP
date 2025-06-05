package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Post
import com.esteban.ruano.models.blog.PostResponse
import com.esteban.ruano.models.tasks.PostDTO

fun PostResponse.toDTO(): PostDTO {
    return PostDTO(
        id = this.id,
        title = this.title,
        slug = this.slug,
        imageUrl = this.imageUrl,
        description = this.description,
        tags = this.tags,
        category = this.category,
        publishedDate = this.publishedDate
    )
}

fun Post.toDTO(): PostDTO {
    return PostDTO(
        id = this.id.value.toString(),
        title = this.title,
        slug = this.slug,
        imageUrl = this.imageUrl,
        description = this.description,
        tags = this.categories,
        category = this.category,
        publishedDate = this.publishedDate.toString()
    )
}