package com.esteban.ruano.lifecommander.data.repository


import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.models.Local
import com.esteban.ruano.core_data.models.Remote
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.lifecommander.data.datasource.SyncDataSource
import com.esteban.ruano.lifecommander.database.SyncDTO
import com.esteban.ruano.lifecommander.domain.repository.SyncRepository

class SyncRepositoryImpl (
    @Local private val localDataSource: SyncDataSource,
    @Remote private val remoteDataSource: SyncDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences 
): BaseRepository(), SyncRepository {
    override suspend fun getLocalSyncData(lastSyncTimestamp: Long): Result<SyncDTO> {
        return doLocalRequest {
            localDataSource.getSyncData(
                lastSyncTimestamp = lastSyncTimestamp
            )
        }
    }

    override suspend fun sync(localSync: SyncDTO): Result<SyncDTO> {
        return doRemoteRequest(networkHelper.isNetworkAvailable()) {
            remoteDataSource.sync(localSync) ?: throw Exception("Error syncing data")
        }
    }
}