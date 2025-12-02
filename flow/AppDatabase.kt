package com.example.mids.flow

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mids.alerts.AlertDao
import com.example.mids.alerts.AlertEntity

@Database(entities = [FlowEntity::class, AlertEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flowDao(): FlowDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun init(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "mids.db"
                    ).build()
                }
            }
        }

        fun get(): AppDatabase {
            return INSTANCE ?: throw IllegalStateException("DB not initialized")
        }
    }
}
