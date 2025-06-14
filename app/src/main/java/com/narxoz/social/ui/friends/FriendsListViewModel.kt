package com.narxoz.social.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendsListViewModel(
    private val repo: FriendsRepository = FriendsRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(FriendsListState(isLoading = true))
    val state: StateFlow<FriendsListState> = _state.asStateFlow()

    init { reload() }

    fun changeTab(tab: FriendsTab) {
        _state.update { it.copy(tab = tab) }
    }

    fun updateFilter(text: String) {
        _state.update { it.copy(filter = text) }
    }

    fun reload() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        val friends = repo.list()
        val incoming = repo.incoming()
        val outgoing = repo.outgoing()
        val error = friends.exceptionOrNull()?.message
            ?: incoming.exceptionOrNull()?.message
            ?: outgoing.exceptionOrNull()?.message
        _state.update {
            it.copy(
                isLoading = false,
                error = error,
                friends = friends.getOrElse { emptyList() },
                incoming = incoming.getOrElse { emptyList() },
                outgoing = outgoing.getOrElse { emptyList() },
            )
        }
    }

    fun cancelRequest(id: Int) = viewModelScope.launch {
        repo.cancel(id)
        reload()
    }

    fun removeFriend(id: Int) = viewModelScope.launch {
        repo.remove(id)
        reload()
    }
}