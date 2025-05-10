package com.esteban.ruano.core_data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.esteban.ruano.core_data.local.model.HistoryTrack

@Dao
interface HistoryTrackDao {

    @Query("SELECT * FROM history_track")
    suspend fun getHistoryTracks(): List<HistoryTrack>

    @Query("SELECT * FROM history_track WHERE timestamp > :lastSyncTimestamp")
    suspend fun getHistoryTracks(lastSyncTimestamp: Long): List<HistoryTrack>

    @Query("SELECT * FROM history_track WHERE id = :id")
    suspend fun getHistoryTrack(id: Int): HistoryTrack

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryTrack(historyTrack: HistoryTrack)

    @Update
    suspend fun updateHistoryTrack(historyTrack: HistoryTrack)
}
