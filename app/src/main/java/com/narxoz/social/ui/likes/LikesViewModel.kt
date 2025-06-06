package com.narxoz.social.ui.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.LikesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class LikesViewModel(
    private val postId: Int,
    private val repo: LikesRepository = LikesRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(LikesState(isLoading = true))
    val state: StateFlow<LikesState> = _state.asStateFlow()

    init { reload() }

    fun reload() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        repo.load(postId)
            .onSuccess { list ->
                _state.update { it.copy(isLoading = false, likes = list, error = null) }
            }
            .onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Ошибка сети") }
            }
    }
}