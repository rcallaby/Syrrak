package com.example.mids.net

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.example.mids.ui.MainActivity
import kotlinx.coroutines.*
import java.io.FileInputStream
import kotlin.coroutines.CoroutineContext

class CaptureVpnService : VpnService(), CoroutineScope {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithNotification()
    }

    override fun onDestroy() {
        job.cancel()
        vpnInterface?.close()
        super.onDestroy()
    }

    private fun startForegroundServiceWithNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, "mids_channel")
            .setContentTitle("mIDS capture")
            .setContentText("Capturing network traffic")
            .setContentIntent(pending)
            .build()
        startForeground(1, notification)
    }

    fun startCapture() {
        val builder = Builder()
        builder.addAddress("10.0.0.2", 32)
        builder.addRoute("0.0.0.0", 0)
        builder.setSession("mIDS-proto")
        vpnInterface = builder.establish()

        vpnInterface?.let { pfd ->
            launch {
                readPackets(pfd)
            }
        }
    }

    fun stopCapture() {
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
    }

    private suspend fun readPackets(pfd: ParcelFileDescriptor) {
        val input = FileInputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32768)
        try {
            while (isActive) {
                val length = input.read(buffer)
                if (length > 0) {
                    val packet = buffer.copyOf(length)
                    PacketDispatcher.dispatch(packet)
                } else {
                    delay(10)
                }
            }
        } catch (e: Exception) {
            // todo: proper logging
        } finally {
            try { input.close() } catch (_: Exception) {}
        }
    }
}
