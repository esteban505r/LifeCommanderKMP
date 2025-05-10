package com.esteban.ruano.lifecommander.data.datasource

import com.esteban.ruano.lifecommander.data.mappers.toDomainModel
import com.esteban.ruano.lifecommander.data.mappers.toResponseModel
import com.esteban.ruano.lifecommander.data.remote.SyncApi
import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.lifecommander.domain.model.HistoryTrack

class SyncRemoteDataSource(
    val syncApi: SyncApi
) : SyncDataSource {
    override suspend fun getSyncData(
        lastSyncTimestamp: Long
    ):SyncDTO = syncApi.getSyncData(
        lastSyncTimestamp
    ).toDomainModel()

    override suspend fun sync(localSync: SyncDTO): SyncDTO? {
        return syncApi.saveSyncData(
            localSync.toResponseModel()
        )?.toDomainModel()
    }
}
