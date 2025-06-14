package com.narxoz.social.ui.profile

import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    fun setNickname(text: String) { _state.update { it.copy(nickname = text) } }
    fun setAvatar(uri: android.net.Uri?) { _state.update { it.copy(avatarUri = uri) } }

    fun save() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        val file = _state.value.avatarUri?.toFile()
        repo.update(_state.value.nickname.takeIf { it.isNotBlank() }, file)
            .onSuccess { _state.update { it.copy(isLoading = false) } }
            .onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Ошибка") }
            }
    }
}
