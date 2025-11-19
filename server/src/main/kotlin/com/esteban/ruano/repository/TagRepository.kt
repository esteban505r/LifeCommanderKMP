package com.esteban.ruano.repository

import com.esteban.ruano.database.converters.toDTO
import com.esteban.ruano.models.tags.CreateTagDTO
import com.esteban.ruano.models.tags.TagDTO
import com.esteban.ruano.models.tags.UpdateTagDTO
import com.esteban.ruano.service.TagService
import java.util.*

class TagRepository(private val tagService: TagService) {

    fun createTag(userId: Int, createTagDTO: CreateTagDTO): UUID? {
        return tagService.createTag(userId, createTagDTO)
    }

    fun getTagById(userId: Int, tagId: UUID): TagDTO? {
        return tagService.getTagById(userId, tagId)?.toDTO()
    }

    fun getTagBySlug(userId: Int, slug: String): TagDTO? {
        return tagService.getTagBySlug(userId, slug)?.toDTO()
    }

    fun listTags(userId: Int): List<TagDTO> {
        return tagService.listTags(userId).map { it.toDTO() }
    }

    fun updateTag(userId: Int, tagId: UUID, updateTagDTO: UpdateTagDTO): Boolean {
        return tagService.updateTag(userId, tagId, updateTagDTO)
    }

    fun deleteTag(userId: Int, tagId: UUID): Boolean {
        return tagService.deleteTag(userId, tagId)
    }

    fun attachTagToTask(userId: Int, taskId: UUID, tagId: UUID): Boolean {
        return tagService.attachTagToTask(userId, taskId, tagId)
    }

    fun detachTagFromTask(userId: Int, taskId: UUID, tagId: UUID): Boolean {
        return tagService.detachTagFromTask(userId, taskId, tagId)
    }

    fun replaceTaskTags(userId: Int, taskId: UUID, tagIds: List<UUID>): Boolean {
        return tagService.replaceTaskTags(userId, taskId, tagIds)
    }

    fun findTasksByTag(userId: Int, tagSlug: String, limit: Int, offset: Long): List<com.esteban.ruano.models.tasks.TaskDTO> {
        return tagService.findTasksByTag(userId, tagSlug, limit, offset)
            .map { it.toDTO() }
    }
}

