package com.esteban.ruano.repository

import com.esteban.ruano.models.habits.*
import com.esteban.ruano.models.sync.SyncDTO
import com.esteban.ruano.service.HabitService
import com.esteban.ruano.service.SyncService
import parseDate
import parseDateTime

class SyncRepository(private val syncService: SyncService) {

    fun sync(userId:Int,localSyncDTO: SyncDTO):SyncDTO {
        return syncService.sync(userId,localSyncDTO)
    }

}