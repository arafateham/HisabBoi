package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val morningReminderTime: String = "10:00",
    val nightReminderTime: String = "21:30",
    val morningReminderEnabled: Boolean = true,
    val nightReminderEnabled: Boolean = true,
    val weekendDay: Int = 4,        // 4 = Friday
    val weekStart: Int = 6,         // 6 = Saturday
    val monthlyBudget: Long = 0L,   // in paisa (x100)
    val darkMode: Boolean = true,
    val userId: String = "default_user",
    val userName: String = "ইউজার",
    val userEmail: String = "user@hisabboi.app",
    val isLoggedIn: Boolean = false,
    val lastSyncTime: Long = 0L
) {
    val isDarkTheme: Boolean get() = darkMode
    val dailyReminderEnabled: Boolean get() = nightReminderEnabled
    val reminderHour: Int
        get() = try {
            nightReminderTime.split(":")[0].toInt()
        } catch (e: Exception) {
            21
        }
    val reminderMinute: Int
        get() = try {
            nightReminderTime.split(":")[1].toInt()
        } catch (e: Exception) {
            30
        }
}

class UserPreferences(private val context: Context) {
    companion object {
        val MORNING_TIME = stringPreferencesKey("morning_reminder_time")
        val NIGHT_TIME = stringPreferencesKey("night_reminder_time")
        val MORNING_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val NIGHT_ENABLED = booleanPreferencesKey("night_reminder_enabled")
        val WEEKEND_DAY = intPreferencesKey("weekend_day")
        val WEEK_START = intPreferencesKey("week_start")
        val MONTHLY_BUDGET = longPreferencesKey("monthly_budget")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            morningReminderTime = preferences[MORNING_TIME] ?: "10:00",
            nightReminderTime = preferences[NIGHT_TIME] ?: "21:30",
            morningReminderEnabled = preferences[MORNING_ENABLED] ?: true,
            nightReminderEnabled = preferences[NIGHT_ENABLED] ?: true,
            weekendDay = preferences[WEEKEND_DAY] ?: 4,
            weekStart = preferences[WEEK_START] ?: 6,
            monthlyBudget = preferences[MONTHLY_BUDGET] ?: 0L,
            darkMode = preferences[DARK_MODE] ?: true,
            userId = preferences[USER_ID] ?: "default_user",
            userName = preferences[USER_NAME] ?: "ইউজার",
            userEmail = preferences[USER_EMAIL] ?: "user@hisabboi.app",
            isLoggedIn = preferences[IS_LOGGED_IN] ?: false,
            lastSyncTime = preferences[LAST_SYNC_TIME] ?: 0L
        )
    }

    suspend fun updateMorningReminder(time: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MORNING_TIME] = time
            prefs[MORNING_ENABLED] = enabled
        }
    }

    suspend fun updateNightReminder(time: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NIGHT_TIME] = time
            prefs[NIGHT_ENABLED] = enabled
        }
    }

    suspend fun updateMonthlyBudget(budgetPaisa: Long) {
        context.dataStore.edit { prefs ->
            prefs[MONTHLY_BUDGET] = budgetPaisa
        }
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    suspend fun updateUserData(id: String, name: String, email: String, loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
            prefs[IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun updateLastSyncTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME] = time
        }
    }
}
