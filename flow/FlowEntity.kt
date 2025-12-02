package com.example.mids.flow

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flows")
data class FlowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flowKey: String, // 5-tuple hash
    val srcIp: String,
    val dstIp: String,
    val srcPort: Int?,
    val dstPort: Int?,
    val protocol: Int,
    val startTs: Long,
    val lastTs: Long,
    val bytes: Long,
    val packets: Int
)
