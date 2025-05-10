package com.esteban.ruano.core_data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = HistoryTrack.TABLE_NAME)
data class HistoryTrack(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "entity_name") val entityName: String,
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "action_type") val actionType: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "isSynced") val isSynced: Boolean = false,
    @ColumnInfo(name = "isLocal") val isLocal: Boolean = true
){
    companion object{
        const val TABLE_NAME = "history_track"
    }
}
