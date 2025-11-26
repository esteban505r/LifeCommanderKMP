package com.esteban.ruano.models.study

import kotlinx.serialization.Serializable

@Serializable
data class StudyTopicDTO(
    val id: String,
    val name: String,
    val description: String? = null,
    val discipline: StudyDisciplineDTO? = null, // Expose discipline object instead of disciplineId
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateStudyTopicDTO(
    val name: String,
    val description: String? = null,
    val disciplineId: String? = null, // Accept disciplineId for creation
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class UpdateStudyTopicDTO(
    val name: String? = null,
    val description: String? = null,
    val disciplineId: String? = null, // Accept disciplineId for updates
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean? = null
)

