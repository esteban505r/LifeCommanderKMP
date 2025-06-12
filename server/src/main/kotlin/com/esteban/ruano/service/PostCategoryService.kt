package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.database.entities.PostCategory
import com.esteban.ruano.database.entities.PostCategories
import com.esteban.ruano.models.blog.PostCategoryDTO
import com.esteban.ruano.models.blog.CreatePostCategoryDTO
import com.esteban.ruano.models.blog.UpdatePostCategoryDTO
import com.esteban.ruano.utils.SecurityUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class PostCategoryService : BaseService() {

    private fun hashPassword(password: String): String {
        return at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun getAllCategories(activeOnly: Boolean = true): List<PostCategoryDTO> {
        return transaction {
            val query = if (activeOnly) {
                PostCategories.status eq com.esteban.ruano.database.models.Status.ACTIVE
            } else {
                org.jetbrains.exposed.sql.Op.TRUE
            }
            
            PostCategory.find { query }
                .orderBy(PostCategories.displayOrder to SortOrder.ASC)
                .map { it.toDTO() }
        }
    }

    fun getCategoryById(id: UUID): PostCategoryDTO? {
        return transaction {
            PostCategory.findById(id)?.toDTO()
        }
    }

    fun getCategoryBySlug(slug: String): PostCategoryDTO? {
        return transaction {
            PostCategory.find { PostCategories.slug eq slug }.firstOrNull()?.toDTO()
        }
    }

    fun createCategory(categoryDTO: CreatePostCategoryDTO): UUID {
        return transaction {
            PostCategory.new {
                name = categoryDTO.name
                slug = categoryDTO.slug
                description = categoryDTO.description
                color = categoryDTO.color
                icon = categoryDTO.icon
                displayOrder = categoryDTO.displayOrder
                password = categoryDTO.password?.let { hashPassword(it) }
            }.id.value
        }
    }

    fun updateCategory(id: UUID, updateDTO: UpdatePostCategoryDTO): Boolean {
        return transaction {
            val category = PostCategory.findById(id) ?: return@transaction false
            
            updateDTO.name?.let { category.name = it }
            updateDTO.slug?.let { category.slug = it }
            updateDTO.description?.let { category.description = it }
            updateDTO.color?.let { category.color = it }
            updateDTO.icon?.let { category.icon = it }
            updateDTO.displayOrder?.let { category.displayOrder = it }
            
            // Handle password update with hashing
            updateDTO.password?.let { newPassword ->
                category.password = if (newPassword.isEmpty()) null else hashPassword(newPassword)
            }
            
            true
        }
    }

    fun deleteCategory(id: UUID): Boolean {
        return transaction {
            val category = PostCategory.findById(id) ?: return@transaction false
            category.status = com.esteban.ruano.database.models.Status.DELETED
            true
        }
    }

    fun hardDeleteCategory(id: UUID): Boolean {
        return transaction {
            val category = PostCategory.findById(id) ?: return@transaction false
            category.delete()
            true
        }
    }

    fun verifyCategoryPassword(id: UUID, password: String): Boolean {
        return transaction {
            val category = PostCategory.findById(id) ?: return@transaction false
            category.password?.let { hashedPassword ->
                SecurityUtils.checkPassword(password, hashedPassword)
            } ?: false
        }
    }

    fun verifyCategoryPasswordBySlug(slug: String, password: String): Boolean {
        return transaction {
            val category = PostCategory.find { PostCategories.slug eq slug }.firstOrNull() 
                ?: return@transaction false
            category.password?.let { hashedPassword ->
                SecurityUtils.checkPassword(password, hashedPassword)
            } ?: false
        }
    }
} 