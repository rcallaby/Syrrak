package com.example.mids.features

import com.example.mids.flow.FlowEntity
import com.example.mids.flow.AppDatabase
import com.example.mids.net.SimpleIpParser
import com.example.mids.detection.DetectionPipeline
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

data class FlowFeatures(
    val flowKey: String,
    val deviceIdHash: String,
    val windowStart: Long,
    val pkts: Int,
    val bytes: Int,
    val avgPktSize: Double,
    val avgIATMs: Double,
    val uniqueDstCount: Int,
    val protocol: Int,
    val srcIp: String,
    val dstIp: String,
    val srcPort: Int?,
    val dstPort: Int?
)

private data class FlowAggregate(
    var startTs: Long,
    var lastTs: Long,
    var bytes: Long,
    var pkts: Int,
    var lastArrivalMs: Long
) {
    fun toFeatures(key: String, srcIp: String, dstIp: String, srcPort: Int?, dstPort: Int?, protocol: Int): FlowFeatures {
        val duration = max(1L, lastTs - startTs)
        val avgPkt = if (pkts > 0) bytes.toDouble() / pkts else 0.0
        val avgIAT = if (pkts > 1) duration.toDouble() / (pkts - 1) else duration.toDouble()
        return FlowFeatures(
            flowKey = key,
            deviceIdHash = "local", // replace with real device id hash
            windowStart = startTs,
            pkts = pkts,
            bytes = bytes.toInt(),
            avgPktSize = avgPkt,
            avgIATMs = avgIAT,
            uniqueDstCount = 1,
            protocol = protocol,
            srcIp = srcIp,
            dstIp = dstIp,
            srcPort = srcPort,
            dstPort = dstPort
        )
    }
}

object FeatureExtractor {
    private val activeFlows = ConcurrentHashMap<String, FlowAggregate>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // Emit windows every 10s
        scope.launch {
            while (isActive) {
                delay(10_000)
                emitWindow()
            }
        }
    }

    fun onPacket(ipPacket: SimpleIpParser.IpPacket, srcPort: Int?, dstPort: Int?) {
        val key = buildFlowKey(ipPacket.srcAddress, ipPacket.dstAddress, srcPort, dstPort, ipPacket.protocol)
        val now = System.currentTimeMillis()
        val agg = activeFlows.compute(key) { _, existing ->
            if (existing == null) FlowAggregate(now, now, ipPacket.payload.size.toLong(), 1, now)
            else {
                existing.bytes += ipPacket.payload.size
                existing.pkts += 1
                existing.lastTs = now
                existing.lastArrivalMs = now
                existing
            }
        }
        // persist minimal flow entity to DB asynchronously
        scope.launch {
            try {
                val flow = FlowEntity(
                    flowKey = key,
                    srcIp = ipPacket.srcAddress,
                    dstIp = ipPacket.dstAddress,
                    srcPort = srcPort,
                    dstPort = dstPort,
                    protocol = ipPacket.protocol,
                    startTs = agg!!.startTs,
                    lastTs = agg.lastTs,
                    bytes = agg.bytes,
                    packets = agg.pkts
                )
                AppDatabase.get().flowDao().insert(flow)
            } catch (_: Exception) {}
        }
    }

    private fun emitWindow() {
        val now = System.currentTimeMillis()
        val features = mutableListOf<FlowFeatures>()
        val iterator = activeFlows.entries.iterator()
        while (iterator.hasNext()) {
            val (k, agg) = iterator.next()
            // For scaffold: we don't have src/dst ports stored separately here; use placeholders or persist more info above
            val parts = k.split("|")
            val src = parts.getOrNull(0) ?: "0.0.0.0"
            val dst = parts.getOrNull(1) ?: "0.0.0.0"
            val protocol = parts.getOrNull(4)?.toIntOrNull() ?: 0
            val srcPort = parts.getOrNull(2)?.toIntOrNull()
            val dstPort = parts.getOrNull(3)?.toIntOrNull()
            features.add(agg.toFeatures(k, src, dst, srcPort, dstPort, protocol))
            // prune flows not active in the last 60s
            if (now - agg.lastArrivalMs > 60_000) iterator.remove()
        }
        if (features.isNotEmpty()) {
            DetectionPipeline.onFeatures(features)
        }
    }

    private fun buildFlowKey(src: String, dst: String, srcPort: Int?, dstPort: Int?, proto: Int): String {
        // deterministic string key: src|dst|srcPort|dstPort|proto
        return listOf(src, dst, srcPort?.toString() ?: "_", dstPort?.toString() ?: "_", proto.toString()).joinToString("|")
    }
}
