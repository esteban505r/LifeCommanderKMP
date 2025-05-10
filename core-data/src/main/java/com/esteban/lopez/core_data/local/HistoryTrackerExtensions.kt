package com.esteban.ruano.core_data.local

import com.esteban.ruano.core_data.local.model.DBActions
import com.esteban.ruano.core_data.local.model.HistoryTrack

suspend fun HistoryTrackDao.addInsertOperation(entityName: String, entityId: String) {
    insertHistoryTrack(
        HistoryTrack(
            entityName = entityName,
            entityId = entityId,
            actionType = DBActions.INSERT.value,
            timestamp = System.currentTimeMillis(),
        )
    )
}

suspend fun HistoryTrackDao.addUpdateOperation(entityName: String, entityId: String) {
    insertHistoryTrack(
        HistoryTrack(
            entityName = entityName,
            entityId = entityId,
            actionType = DBActions.UPDATE.value,
            timestamp = System.currentTimeMillis(),
        )
    )
}

suspend fun HistoryTrackDao.addDeleteOperation(entityName: String, entityId: String) {
    insertHistoryTrack(
        HistoryTrack(
            entityName = entityName,
            entityId = entityId,
            actionType = DBActions.DELETE.value,
            timestamp = System.currentTimeMillis(),
        )
    )
}
