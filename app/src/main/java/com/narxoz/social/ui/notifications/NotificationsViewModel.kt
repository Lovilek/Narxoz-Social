package com.narxoz.social.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.NotificationsRepository
import com.narxoz.social.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repo: NotificationsRepository = NotificationsRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState(isLoading = true))
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init { reload() }

    fun reload() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        repo.list()
            .onSuccess { list ->
                _state.update { it.copy(isLoading = false, notifications = list) }
            }
            .onFailure { e ->
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Ошибка сети")
                }
            }
    }

    fun markRead(id: Int) = viewModelScope.launch {
        repo.markRead(id)
        _state.update { st ->
            st.copy(notifications = st.notifications.map { n ->
                if (n.id == id) n.copy(isRead = true) else n
            })
        }
    }
    fun respondToFriendRequest(friendId: Int, accepted: Boolean, notifId: Int) =
        viewModelScope.launch {
            friendsRepo.respond(friendId, accepted)
            markRead(notifId)
        }
}