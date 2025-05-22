package com.esteban.ruano.models.finance

import kotlinx.serialization.Serializable


@Serializable
data class KeywordResponseDTO(
    val id: String,
    val keyword: String,
)