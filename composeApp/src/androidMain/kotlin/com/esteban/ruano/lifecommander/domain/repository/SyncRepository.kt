package com.esteban.ruano.lifecommander.domain.repository

import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.lifecommander.domain.model.HistoryTrack

interface SyncRepository {
    suspend fun getLocalSyncData(
        lastSyncTimestamp: Long
    ):Result<SyncDTO>
    suspend fun sync(localSync: SyncDTO):Result<SyncDTO>
}