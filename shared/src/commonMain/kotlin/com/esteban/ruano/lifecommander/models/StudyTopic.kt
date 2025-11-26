package com.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class StudyTopic(
    val id: String,
    val name: String,
    val description: String? = null,
    val discipline: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

