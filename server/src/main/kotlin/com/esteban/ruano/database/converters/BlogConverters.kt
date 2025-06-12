package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Post
import com.esteban.ruano.database.entities.PostCategory
import com.esteban.ruano.models.blog.PostResponse
import com.esteban.ruano.models.blog.PostCategoryDTO
import com.esteban.ruano.models.tasks.PostDTO
import com.esteban.ruano.utils.SecurityUtils

fun PostResponse.toDTO(): PostDTO {
    return PostDTO(
        id = this.id,
        title = this.title,
        slug = this.slug,
        imageUrl = this.imageUrl,
        description = this.description,
        tags = this.tags,
        category = this.category,
        publishedDate = this.publishedDate,
        requiresPassword = this.category.requiresPassword
    )
}

fun Post.toDTO(passwordProvided: String? = null): PostDTO {
    val hasPassword = this.password != null || this.category.password != null
    val hasCorrectPassword = if (hasPassword) this.verifyPassword(passwordProvided) else true
    
    return PostDTO(
        id = this.id.value.toString(),
        title = this.title,
        slug = this.slug,
        imageUrl = this.imageUrl,
        description = this.description,
        tags = this.tags,
        category = this.category.toDTO(),
        publishedDate = this.publishedDate.toString(),
        requiresPassword = hasPassword && !hasCorrectPassword
    )
}

fun PostCategory.toDTO(): PostCategoryDTO {
    return PostCategoryDTO(
        id = this.id.value.toString(),
        name = this.name,
        slug = this.slug,
        description = this.description,
        color = this.color,
        icon = this.icon,
        displayOrder = this.displayOrder,
        requiresPassword = this.password != null,
        createdDate = this.createdDate.toString(),
        updatedDate = this.updatedDate.toString()
    )
}

fun Post.isPasswordProtected(): Boolean {
    return this.password != null || this.category.password != null
}

fun Post.getEffectivePassword(): String? {
    return this.password ?: this.category.password
}

fun Post.verifyPassword(providedPassword: String?): Boolean {
    val effectivePassword = this.getEffectivePassword()
    return when {
        effectivePassword == null -> true
        providedPassword == null -> false
        else -> SecurityUtils.checkPassword(providedPassword, effectivePassword) // Secure BCrypt verification
    }
}