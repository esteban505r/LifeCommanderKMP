package com.esteban.ruano.repository

import com.esteban.ruano.models.sync.SyncDTO
import com.esteban.ruano.service.SyncService

class SyncRepository(private val syncService: SyncService) {

    fun sync(userId:Int,localSyncDTO: SyncDTO):SyncDTO {
        return syncService.sync(userId,localSyncDTO)
    }

}