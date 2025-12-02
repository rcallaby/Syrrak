package com.example.mids.alerts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlertDao {
    @Insert
    suspend fun insert(alert: AlertEntity): Long

    @Query("SELECT * FROM alerts ORDER BY ts DESC LIMIT 200")
    suspend fun recent(): List<AlertEntity>

    @Query("DELETE FROM alerts WHERE ts < :cutoff")
    suspend fun pruneOlderThan(cutoff: Long)
}
