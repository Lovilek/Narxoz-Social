package com.narxoz.social.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.api.NotificationDto
import com.narxoz.social.network.NotificationWsManager
import com.narxoz.social.repository.AuthRepository
import com.narxoz.social.repository.NotificationsRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class NotificationsViewModel(
    private val repo: NotificationsRepository = NotificationsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState(isLoading = true))
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        reload()
        val jwt = AuthRepository.getAccessToken() ?: ""
        NotificationWsManager.connect(jwt, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val moshi = Moshi.Builder().build()
                val adapter = moshi.adapter(NotificationDto::class.java)
                val notif = adapter.fromJson(text) ?: return
                viewModelScope.launch {
                    _state.update { s ->
                        s.copy(
                            list = listOf(notif) + s.list,
                            unread = s.unread + 1
                        )
                    }
                }
            }
        })
    }

    fun reload() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        repo.list()
            .onSuccess { list ->
                val unread = list.count { !it.isRead }
                _state.update { it.copy(list = list, unread = unread, isLoading = false) }
            }
            .onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Network error", isLoading = false) }
            }
    }

    fun markRead(id: Int) = viewModelScope.launch {
        repo.markRead(id)
        _state.update { s ->
            val newList = s.list.map { if (it.id == id) it.copy(isRead = true) else it }
            val unread = newList.count { !it.isRead }
            s.copy(list = newList, unread = unread)
        }
    }

    override fun onCleared() {
        NotificationWsManager.close()
    }
}
