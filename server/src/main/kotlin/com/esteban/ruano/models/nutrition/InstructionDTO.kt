package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class InstructionDTO(
    val id: String? = null,
    val stepNumber: Int,
    val description: String
) 