package com.example.corsa.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class NotificationPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val weeklyNotificationEnabled: Flow<Boolean> =
        dataStore.data.map { it[WEEKLY_KEY] ?: false }

    suspend fun setWeeklyNotificationEnabled(enabled: Boolean) =
        dataStore.edit { it[WEEKLY_KEY] = enabled }

    companion object {
        private val WEEKLY_KEY = booleanPreferencesKey("weekly_challenge_enabled")
    }
}