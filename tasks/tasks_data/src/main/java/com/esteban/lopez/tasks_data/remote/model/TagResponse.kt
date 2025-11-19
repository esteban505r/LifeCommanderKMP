package com.esteban.ruano.tasks_data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class TagResponse(
    val id: String,
    val name: String,
    val color: String? = null,
    val slug: String? = null
)

