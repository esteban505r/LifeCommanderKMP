package com.esteban.ruano.lifecommander.data.datasource

import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.lifecommander.domain.model.HistoryTrack

interface SyncDataSource {
    suspend fun getSyncData(
        lastSyncTimestamp: Long
    ):SyncDTO

    suspend fun sync(localSync: SyncDTO):SyncDTO?
}
