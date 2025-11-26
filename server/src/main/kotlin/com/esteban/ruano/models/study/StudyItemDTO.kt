package com.esteban.ruano.models.study

import kotlinx.serialization.Serializable

@Serializable
data class StudyItemDTO(
    val id: String,
    val topic: StudyTopicDTO? = null, // Expose topic object instead of topicId
    val title: String,
    val obsidianPath: String? = null,
    val stage: String, // PENDING, IN_PROGRESS, PROCESSED
    val modeHint: String? = null, // INPUT, PROCESSING, REVIEW
    val discipline: String? = null,
    val progress: Int = 0, // 0-100
    val estimatedEffortMinutes: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateStudyItemDTO(
    val topicId: String? = null,
    val title: String,
    val obsidianPath: String? = null,
    val stage: String = "PENDING",
    val modeHint: String? = null,
    val discipline: String? = null,
    val progress: Int = 0,
    val estimatedEffortMinutes: Int? = null
)

@Serializable
data class UpdateStudyItemDTO(
    val topicId: String? = null,
    val title: String? = null,
    val obsidianPath: String? = null,
    val stage: String? = null,
    val modeHint: String? = null,
    val discipline: String? = null,
    val progress: Int? = null,
    val estimatedEffortMinutes: Int? = null
)

