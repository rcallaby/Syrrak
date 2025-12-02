package com.example.mids.net

import java.net.InetAddress
import java.nio.ByteBuffer

object SimpleIpParser {
    data class IpPacket(
        val version: Int,
        val protocol: Int,
        val srcAddress: String,
        val dstAddress: String,
        val payload: ByteArray
    )

    fun parse(packet: ByteArray): IpPacket? {
        if (packet.isEmpty()) return null
        val bb = ByteBuffer.wrap(packet)
        val versionAndIhl = bb.get().toInt() and 0xFF
        val version = (versionAndIhl shr 4) and 0xF
        if (version != 4) return null
        val ihl = versionAndIhl and 0xF
        if (ihl < 5) return null
        // total length check is omitted (scaffold)
        // read proto at offset 9
        val proto = packet[9].toInt() and 0xFF
        val src = ByteArray(4)
        val dst = ByteArray(4)
        System.arraycopy(packet, 12, src, 0, 4)
        System.arraycopy(packet, 16, dst, 0, 4)
        val headerLen = ihl * 4
        val payload = if (packet.size > headerLen) packet.copyOfRange(headerLen, packet.size) else ByteArray(0)
        val srcAddr = InetAddress.getByAddress(src).hostAddress
        val dstAddr = InetAddress.getByAddress(dst).hostAddress
        return IpPacket(version, proto, srcAddr, dstAddr, payload)
    }
}
