package com.example.mids

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {
    // Application-wide coroutine scope
    val appScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // Initialize DB
        com.example.mids.flow.AppDatabase.init(applicationContext)
    }
}
