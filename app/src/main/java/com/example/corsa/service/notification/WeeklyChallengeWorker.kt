package com.example.corsa.service.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.corsa.R

class WeeklyChallengeWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showNotification()
        WeeklyChallengeScheduler.schedule(WorkManager.getInstance(applicationContext))
        return Result.success()
    }
    private fun showNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sprint)
            .setContentTitle("Weekly Challenge Reset!")
            .setContentText("A new weekly challenge is ready. Let's go! 🏃")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "weekly_challenge_notification"
        private const val CHANNEL_ID = "weekly_challenge_channel"
        private const val NOTIFICATION_ID = 2
    }
}