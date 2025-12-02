package com.example.mids.net

import com.example.mids.features.FeatureExtractor
import com.example.mids.net.SimpleIpParser
import java.nio.ByteBuffer

object PacketDispatcher {
    /**
     * Accept raw IP packet bytes from TUN and dispatch parsed result to feature extractor.
     */
    fun dispatch(packet: ByteArray) {
        val ip = SimpleIpParser.parse(packet) ?: return
        // Try to parse transport ports (simple: TCP/UDP)
        var srcPort: Int? = null
        var dstPort: Int? = null
        try {
            val payload = ip.payload
            if (ip.protocol == 6 || ip.protocol == 17) { // TCP=6 UDP=17
                val bb = ByteBuffer.wrap(payload)
                if (payload.size >= 4) {
                    srcPort = bb.short.toInt() and 0xFFFF
                    dstPort = bb.short.toInt() and 0xFFFF
                }
            }
        } catch (_: Exception) {}
        FeatureExtractor.onPacket(ip, srcPort, dstPort)
    }
}
