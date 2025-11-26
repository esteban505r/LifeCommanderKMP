package com.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class StudySession(
    val id: String,
    val topic: StudyTopic? = null, // Expose topic object instead of topicId
    val studyItem: StudyItem? = null, // Expose studyItem object instead of studyItemId
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
data class StudyStats(
    val totalTimeByTopic: Map<String, Int> = emptyMap(), // topicId -> minutes
    val totalTimeByMode: Map<String, Int> = emptyMap(), // mode -> minutes
    val itemsByStage: Map<String, Int> = emptyMap() // stage -> count
)

