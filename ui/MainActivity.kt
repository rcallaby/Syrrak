package com.example.mids.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.mids.R
import com.example.mids.net.CaptureVpnService
import androidx.fragment.app.commit
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var vpnServiceIntent: Intent? = null
    private var vpnPrepared = false
    private val VPN_REQ = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // create simple layout with Buttons & container

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            prepareAndStartVpn()
        }
        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            stopVpn()
        }

        // load AlertsFragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.container, AlertsFragment())
            }
        }
    }

    private fun prepareAndStartVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQ)
        } else {
            onActivityResult(VPN_REQ, Activity.RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VPN_REQ && resultCode == Activity.RESULT_OK) {
            // start service
            vpnServiceIntent = Intent(this, CaptureVpnService::class.java)
            ContextCompat.startForegroundService(this, vpnServiceIntent!!)
            // NOTE: service will call startCapture internally via binder or you can start it here
            // For simplicity we'll send an action
            vpnServiceIntent?.action = "START"
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun stopVpn() {
        vpnServiceIntent?.action = "STOP"
        stopService(vpnServiceIntent)
    }
}
