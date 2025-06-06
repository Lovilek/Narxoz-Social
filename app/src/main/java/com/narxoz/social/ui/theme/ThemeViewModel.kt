package com.narxoz.social.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.data.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ThemeRepository(app)

    /** StateFlow<Boolean> для Composable */
    val isDark: StateFlow<Boolean> = repo.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggle() {
        viewModelScope.launch {
            repo.setDark(!isDark.value)
        }
    }
}