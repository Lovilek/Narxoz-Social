package com.narxoz.social.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.FriendsRepository
import com.narxoz.social.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notifRepo: NotificationsRepository = NotificationsRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState(isLoading = true))
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init { reload() }

    /** Загружает уведомления без дополнительных запросов */
    fun reload() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        notifRepo.list()
            .onSuccess { list ->
                _state.update { it.copy(isLoading = false, notifications = list) }
            }
            .onFailure { e ->
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Ошибка сети")
                }
            }
    }

    /** Отметить уведомление прочитанным локально и на сервере */
    fun markRead(notifId: Int) = viewModelScope.launch {
        notifRepo.markRead(notifId)
        _state.update { st ->
            st.copy(
                notifications = st.notifications.map { n ->
                    if (n.id == notifId) n.copy(isRead = true) else n
                }
            )
        }
    }

    /** Принять / отклонить заявку */
    fun respondToFriendRequest(
        requestId: Int,
        accepted: Boolean,
        notifId: Int
    ) = viewModelScope.launch {
        friendsRepo.respond(requestId, accepted)
            .onSuccess { markRead(notifId) }
            .onFailure { e ->
                Log.e("NotificationsVM", "Friend respond failed", e)
                _state.update { it.copy(error = e.message ?: "Не удалось отправить ответ") }
            }
    }
}