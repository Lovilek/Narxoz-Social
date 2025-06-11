package com.narxoz.social.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.network.api.ChatApi
import com.narxoz.social.network.dto.ChatShortDto
import com.narxoz.social.repository.ChatRepository
import com.narxoz.social.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val api : ChatApi,
    private val repo: ChatRepository
) : ViewModel() {

    /* ---------- данные ---------- */
    private val _chats = MutableStateFlow<List<ChatShortDto>>(emptyList())
    val chats: StateFlow<List<ChatShortDto>> = _chats.asStateFlow()

    private val _loading = MutableStateFlow(false)

    /** ← публичный read-only поток */
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** Количество непрочитанных сообщений во всех чатах */
    val unreadCount: StateFlow<Int> = _chats
        .map { list -> list.sumOf { it.unread } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    fun createGroup(name: String, members: List<Int>) {
        // Only teachers and organizations are allowed to create groups
        val role = AuthRepository.getUserRole()
        if (role !in listOf("teacher", "organization")) return

        viewModelScope.launch {
            val parts = members.map { id ->
                MultipartBody.Part.createFormData("members", id.toString())
            }

            val bodyName = RequestBody.create("text/plain".toMediaType(), name)

            runCatching {
                api.createGroup(bodyName, parts)
            }.onSuccess {
                refresh(silent = true)
            }
        }
    }

    /** ---------- refresh теперь public и НЕ suspend ---------- */
    fun refresh(silent: Boolean = false) = viewModelScope.launch {
        if (!silent) _loading.value = true
        _chats.value = api.getAllChats()
        _loading.value = false
    }

    init {
        /** первая подгрузка */
        viewModelScope.launch { refresh() }

        /* обнуляем счётчик, когда ViewModel чата вызвала markRead() */
        viewModelScope.launch {
            repo.read.collect { cid ->
                _chats.update { list ->
                    list.map { if (it.id == cid) it.copy(unread = 0) else it }
                }
            }
        }

        // периодический факт-чек
        viewModelScope.launch {
            while (isActive) {
                delay(15_000)
                refresh(silent = true) // silent = не показываем спиннер
            }
        }

        viewModelScope.launch {
            repo.incoming.collect { msg ->
                val known = _chats.value.any { it.id == msg.chatId }
                if (!known) {
                    refresh(silent = true)
                } else {
                    _chats.update { list ->
                        list.map { chat ->
                            if (chat.id == msg.chatId) {
                                chat.copy(unread = chat.unread + 1)
                            } else chat
                        }
                    }
                }
            }
        }
    }
}