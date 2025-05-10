package com.esteban.ruano.lifecommander.domain.model;

data class HistoryTrack(
    val id: Int = 0,
    val entityName: String,
    val entityId: Int,
    val actionType: String,
    val timestamp: Long,
    val isSynced: Boolean = false,
    val isLocal: Boolean = true
)