package com.narxoz.social.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnotherProfileViewModel(
    private val userId: Int,
    private val repo: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AnotherProfileState(isLoading = true))
    val state: StateFlow<AnotherProfileState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = AnotherProfileState(isLoading = true)
        repo.loadById(userId)
            .onSuccess { prof ->
                _state.value = AnotherProfileState(profile = prof, isLoading = false)
            }
            .onFailure { e ->
                _state.value = AnotherProfileState(isLoading = false, error = e.message ?: "Ошибка")
            }
    }
}

class AnotherProfileVmFactory(private val userId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AnotherProfileViewModel(userId) as T
}
