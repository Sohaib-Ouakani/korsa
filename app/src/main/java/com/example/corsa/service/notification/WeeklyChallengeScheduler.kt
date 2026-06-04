package com.example.corsa.service.notification

import androidx.work.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object WeeklyChallengeScheduler {

    /**
     * Schedules a periodic-like one-shot WorkManager job that fires at the
     * next Monday 00:00 local time. The worker reschedules itself on success,
     * so it stays accurate across DST changes.
     */
    fun schedule(workManager: WorkManager) {
        val delay = millisUntilNextMonday()

        val request = OneTimeWorkRequestBuilder<WeeklyChallengeWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.NONE)
            .addTag(WeeklyChallengeWorker.WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            WeeklyChallengeWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(workManager: WorkManager) {
        workManager.cancelUniqueWork(WeeklyChallengeWorker.WORK_NAME)
    }

    private fun millisUntilNextMonday(): Long {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val nextMonday = now
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())

        return nextMonday.toInstant().toEpochMilli() -
                System.currentTimeMillis()
    }
}