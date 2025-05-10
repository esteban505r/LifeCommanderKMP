package com.esteban.ruano.lifecommander.data.remote

import com.esteban.ruano.lifecommander.data.remote.model.SyncResponse
import com.esteban.ruano.lifecommander.domain.model.HistoryTrack
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApi {
    @GET("sync")
    suspend fun getSyncData(
        @Query("lastSyncTimestamp") lastSyncTimestamp: Long
    ): SyncResponse

    @POST("sync")
    suspend fun saveSyncData(
        @Body syncData: SyncResponse
    ): SyncResponse?
}