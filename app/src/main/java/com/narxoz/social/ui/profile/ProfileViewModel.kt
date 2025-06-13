package com.narxoz.social.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ProfileState(isLoading = true)
        repo.load()
            .onSuccess { prof ->
                _state.value = ProfileState(profile = prof, isLoading = false)
            }
            .onFailure { e ->
                _state.value = ProfileState(isLoading = false, error = e.message ?: "Ошибка")
            }
    }
}
