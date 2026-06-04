package com.example.corsa.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.example.corsa.data.repositories.NotificationPreferencesRepository
import com.example.corsa.data.repositories.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val dataStore = context.applicationContext.dataStore
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = NotificationPreferencesRepository(dataStore)
            if (prefs.weeklyNotificationEnabled.first()) {
                WeeklyChallengeScheduler.schedule(WorkManager.getInstance(context))
            }
        }
    }
}