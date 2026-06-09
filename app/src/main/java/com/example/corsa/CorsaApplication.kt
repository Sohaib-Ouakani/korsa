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