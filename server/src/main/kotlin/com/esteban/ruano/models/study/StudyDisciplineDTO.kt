package com.esteban.ruano.models.study

import kotlinx.serialization.Serializable

@Serializable
data class StudyDisciplineDTO(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val iconUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

