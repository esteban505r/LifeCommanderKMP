package com.esteban.ruano.core_data.local.model

data class SyncItemDTO<T>(
    val item: T,
    val action: String,
    val remoteId: String? = null
)