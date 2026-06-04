package com.example.corsa

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CorsaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startKoin {
            androidLogger()
            androidContext(this@CorsaApplication)
            modules(appModule)
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Existing run tracking channel (move it here if it isn't already)
//        manager.createNotificationChannel(
//            NotificationChannel(
//                "run_tracking_channel",
//                "Run Tracking",
//                NotificationManager.IMPORTANCE_LOW
//            )
//        )

        manager.createNotificationChannel(
            NotificationChannel(
                "weekly_challenge_channel",
                "Weekly Challenge",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you when the weekly challenge resets"
            }
        )
    }
}