package com.example.mids.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.example.mids.features.BluetoothFeatureDispatcher

class BleScanner(private val ctx: Context) {
    private val bluetoothManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? = adapter?.bluetoothLeScanner

    fun startScan() {
        try {
            scanner?.startScan(callback)
        } catch (e: SecurityException) {
            // permission missing, handle in UI
        }
    }

    fun stopScan() {
        try {
            scanner?.stopScan(callback)
        } catch (_: Exception) {}
    }

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val rssi = result.rssi
            val name = result.scanRecord?.deviceName
            val uuids = result.scanRecord?.serviceUuids
            BluetoothFeatureDispatcher.onBleResult(device?.address, rssi, name, uuids)
        }
    }
}
