package com.narxoz.social.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddFriendViewModel(
    private val repo: FriendsRepository = FriendsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AddFriendState())
    val state: StateFlow<AddFriendState> = _state.asStateFlow()

    fun updateInput(text: String) {
        _state.update { it.copy(idInput = text) }
    }

    fun sendRequest() = viewModelScope.launch {
        val id = _state.value.idInput.toIntOrNull() ?: return@launch
        _state.update { it.copy(isLoading = true, error = null, success = false) }
        repo.send(id)
            .onSuccess {
                _state.update { st -> st.copy(isLoading = false, success = true) }
            }
            .onFailure { e ->
                _state.update { st ->
                    st.copy(isLoading = false, error = e.message ?: "Ошибка")
                }
            }
    }
}