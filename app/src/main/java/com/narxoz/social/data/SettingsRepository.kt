package com.narxoz.social.data

import android.content.Context
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

private val KEY_LANGUAGE = stringPreferencesKey("language")
private val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications")
private val KEY_PRIVATE = booleanPreferencesKey("private")

data class Settings(
    val language: String = "en",
    val notifications: Boolean = true,
    val isPrivate: Boolean = false
)

class SettingsRepository(private val context: Context) {

    val settingsFlow: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            language = prefs[KEY_LANGUAGE] ?: "en",
            notifications = prefs[KEY_NOTIFICATIONS] ?: true,
            isPrivate = prefs[KEY_PRIVATE] ?: false
        )
    }

    suspend fun setLanguage(code: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = code
        }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    suspend fun setPrivate(value: Boolean) {
        context.dataStore.edit { it[KEY_PRIVATE] = value }
    }
}
