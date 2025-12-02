package com.example.mids.flow

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FlowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flow: FlowEntity): Long

    @Query("SELECT * FROM flows WHERE flowKey = :key LIMIT 1")
    suspend fun getByKey(key: String): FlowEntity?

    @Query("DELETE FROM flows WHERE lastTs < :cutoff")
    suspend fun pruneOld(cutoff: Long)
}
