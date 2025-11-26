package com.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class StudyItem(
    val id: String,
    val topic: StudyTopic? = null, // Expose topic object instead of topicId
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

