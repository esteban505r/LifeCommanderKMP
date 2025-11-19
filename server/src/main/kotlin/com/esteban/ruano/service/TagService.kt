package com.esteban.ruano.service

import com.esteban.ruano.database.entities.Tag
import com.esteban.ruano.database.entities.Tags
import com.esteban.ruano.database.entities.TaskTags
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.tags.CreateTagDTO
import com.esteban.ruano.models.tags.UpdateTagDTO
import com.esteban.ruano.utils.generateSlug
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.*

class TagService : BaseService() {

    fun createTag(userId: Int, createTagDTO: CreateTagDTO): UUID? {
        return transaction {
            val slug = generateSlug(createTagDTO.name)
            
            // Check if slug already exists for this user
            val existingTag = Tag.find {
                (Tags.user eq userId) and (Tags.slug eq slug)
            }.firstOrNull()
            
            if (existingTag != null) {
                return@transaction null // Slug already exists for this user
            }
            
            Tags.insertOperation(userId) {
                insert {
                    it[name] = createTagDTO.name
                    it[this.slug] = slug
                    it[color] = createTagDTO.color
                    it[user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getTagById(userId: Int, tagId: UUID): Tag? {
        return transaction {
            Tag.find {
                (Tags.id eq tagId) and (Tags.user eq userId)
            }.firstOrNull()
        }
    }

    fun getTagBySlug(userId: Int, slug: String): Tag? {
        return transaction {
            Tag.find {
                (Tags.user eq userId) and (Tags.slug eq slug)
            }.firstOrNull()
        }
    }

    fun listTags(userId: Int): List<Tag> {
        return transaction {
            Tag.find {
                Tags.user eq userId
            }.toList()
        }
    }

    fun updateTag(userId: Int, tagId: UUID, updateTagDTO: UpdateTagDTO): Boolean {
        return transaction {
            val tag = getTagById(userId, tagId) ?: return@transaction false
            
            val newSlug = updateTagDTO.name?.let { generateSlug(it) }
            
            // If name is being updated, check if new slug conflicts
            if (newSlug != null && newSlug != tag.slug) {
                val existingTag = getTagBySlug(userId, newSlug)
                if (existingTag != null && existingTag.id != tagId) {
                    return@transaction false // Slug conflict
                }
            }
            
            Tags.updateOperation(userId) {
                val updatedRows = update({ (Tags.id eq tagId) and (Tags.user eq userId) }) { row ->
                    updateTagDTO.name?.let { 
                        row[name] = it
                        row[slug] = newSlug ?: tag.slug
                    }
                    updateTagDTO.color?.let { row[color] = it }
                }
                if (updatedRows > 0) tagId else null
            } != null
        }
    }

    fun deleteTag(userId: Int, tagId: UUID): Boolean {
        return transaction {
            val tag = getTagById(userId, tagId) ?: return@transaction false
            
            // Delete all task-tag relationships first (CASCADE should handle this, but being explicit)
            TaskTags.deleteWhere {
                (TaskTags.tag eq tagId)
            }
            
            Tags.deleteOperation(userId) {
                val deletedRows = Tags.deleteWhere {
                    (Tags.id eq tagId) and (Tags.user eq userId)
                }
                if (deletedRows > 0) tagId else null
            } != null
        }
    }

    fun attachTagToTask(userId: Int, taskId: UUID, tagId: UUID): Boolean {
        return transaction {
            // Verify task and tag belong to user
            val task = com.esteban.ruano.database.entities.Task.findById(taskId)
            val tag = getTagById(userId, tagId)
            
            if (task == null || tag == null || task.user.id.value != userId) {
                return@transaction false
            }
            
            // Check if relationship already exists
            val existing = TaskTags
                .selectAll().where { (TaskTags.task eq taskId) and (TaskTags.tag eq tagId) }
                .firstOrNull()
            
            if (existing != null) {
                return@transaction true // Already attached
            }
            
            // Create relationship
            TaskTags.insert {
                it[this.task] = taskId
                it[this.tag] = tagId
            }
            true
        }
    }

    fun detachTagFromTask(userId: Int, taskId: UUID, tagId: UUID): Boolean {
        return transaction {
            // Verify task and tag belong to user
            val task = com.esteban.ruano.database.entities.Task.findById(taskId)
            val tag = getTagById(userId, tagId)
            
            if (task == null || tag == null || task.user.id.value != userId) {
                return@transaction false
            }
            
            val deletedRows = TaskTags.deleteWhere {
                (TaskTags.task eq taskId) and (TaskTags.tag eq tagId)
            }
            deletedRows > 0
        }
    }

    fun replaceTaskTags(userId: Int, taskId: UUID, tagIds: List<UUID>): Boolean {
        return transaction {
            val task = com.esteban.ruano.database.entities.Task.findById(taskId)
            if (task == null || task.user.id.value != userId) {
                return@transaction false
            }
            
            // Verify all tags belong to user
            val tags = tagIds.mapNotNull { getTagById(userId, it) }
            if (tags.size != tagIds.size) {
                return@transaction false // Some tags don't exist or don't belong to user
            }
            
            // Remove all existing relationships
            TaskTags.deleteWhere {
                TaskTags.task eq taskId
            }
            
            // Create new relationships
            tagIds.forEach { tagId ->
                TaskTags.insert {
                    it[this.task] = taskId
                    it[tag] = tagId
                }
            }
            
            true
        }
    }

    fun findTasksByTag(userId: Int, tagSlug: String, limit: Int, offset: Long): List<com.esteban.ruano.database.entities.Task> {
        return transaction {
            val tag = getTagBySlug(userId, tagSlug) ?: return@transaction emptyList()
            
            // Get task IDs that have this tag using inSubQuery pattern
            val taskIdsWithTag = TaskTags
                .select(TaskTags.task)
                .where { TaskTags.tag eq tag.id.value }
            
            val tasks = com.esteban.ruano.database.entities.Task.find {
                (com.esteban.ruano.database.entities.Tasks.user eq userId) and
                (com.esteban.ruano.database.entities.Tasks.status eq Status.ACTIVE) and
                (com.esteban.ruano.database.entities.Tasks.id inSubQuery taskIdsWithTag)
            }
            .limit(limit)
            .offset(offset * limit)
            .toList()
            
            // Eagerly load tags for each task to avoid null pointer exceptions
            tasks.forEach { task ->
                // Access tags to trigger lazy loading within the transaction
                task.tags.toList()
            }
            
            tasks
        }
    }
}

