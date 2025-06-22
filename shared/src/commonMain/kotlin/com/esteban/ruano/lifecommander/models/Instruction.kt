package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Instruction(
    val id: String? = null,
    val stepNumber: Int,
    val description: String
) 