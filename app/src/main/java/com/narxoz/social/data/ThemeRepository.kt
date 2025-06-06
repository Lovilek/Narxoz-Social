package com.narxoz.social.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/* — делегат-расширение в контексте приложения — */
private val Context.dataStore by preferencesDataStore(name = "ui_prefs")

private val KEY_DARK = booleanPreferencesKey("dark_theme")

class ThemeRepository(private val context: Context) {

    /** true — тёмная тема, false — светлая; по умолчанию false */
    val themeFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_DARK] ?: false }

    /** сохранить новое значение */
    suspend fun setDark(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK] = value          // prefs — нам доступен в лямбде
        }
    }
}