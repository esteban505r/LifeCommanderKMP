package com.esteban.ruano.models.study

import kotlinx.serialization.Serializable

@Serializable
data class StudySessionDTO(
    val id: String,
    val topic: StudyTopicDTO? = null, // Expose topic object instead of topicId
    val studyItem: StudyItemDTO? = null, // Expose studyItem object instead of studyItemId
    val mode: String, // INPUT, PROCESSING, REVIEW
    val plannedStart: String? = null,
    val plannedEnd: String? = null,
    val actualStart: String? = null,
    val actualEnd: String? = null,
    val durationMinutes: Int? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateStudySessionDTO(
    val topicId: String? = null,
    val studyItemId: String? = null,
    val mode: String, // INPUT, PROCESSING, REVIEW
    val plannedStart: String? = null,
    val plannedEnd: String? = null,
    val actualStart: String? = null,
    val notes: String? = null
)

@Serializable
data class UpdateStudySessionDTO(
    val topicId: String? = null,
    val studyItemId: String? = null,
    val mode: String? = null,
    val plannedStart: String? = null,
    val plannedEnd: String? = null,
    val actualStart: String? = null,
    val actualEnd: String? = null,
    val durationMinutes: Int? = null,
    val notes: String? = null
)

@Serializable
data class StudyStatsDTO(
    val totalTimeByTopic: Map<String, Int> = emptyMap(), // topicId -> minutes
    val totalTimeByMode: Map<String, Int> = emptyMap(), // mode -> minutes
    val itemsByStage: Map<String, Int> = emptyMap() // stage -> count
)

