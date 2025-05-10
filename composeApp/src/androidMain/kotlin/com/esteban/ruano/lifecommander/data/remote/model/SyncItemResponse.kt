package com.esteban.ruano.lifecommander.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncItemResponse<T>(
    val item: T,
    val action: String
)