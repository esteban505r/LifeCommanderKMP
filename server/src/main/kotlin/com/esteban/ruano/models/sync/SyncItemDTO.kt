package com.esteban.ruano.models.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncItemDTO<T>(
    val item: T,
    val action: String,
    val remoteId: String? = null
)