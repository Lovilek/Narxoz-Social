package com.narxoz.social.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    val state: StateFlow<com.narxoz.social.data.Settings> = repo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.narxoz.social.data.Settings())

    fun setLanguage(code: String) {
        viewModelScope.launch { repo.setLanguage(code) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { repo.setNotifications(enabled) }
    }

    fun setPrivate(value: Boolean) {
        viewModelScope.launch { repo.setPrivate(value) }
    }
}
