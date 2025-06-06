package com.narxoz.social.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.network.WsManager
import com.narxoz.social.network.dto.MessageDto
import com.narxoz.social.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /* ---------- состояние ---------- */
    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

    /* ---------- аргумент ---------- */
    private val chatId: String = savedStateHandle["chatId"]
        ?: error("chatId argument is missing")

    /* ---------- инициализация ---------- */
    init {
        // 1️⃣ Подписываемся на поток сообщений из репозитория
        viewModelScope.launch {
            repo.messages.collectLatest { list ->
                _messages.value = list           // ← передаём в UI
            }
        }

        // 2️⃣ Первая подгрузка + отметка «прочитано»
        viewModelScope.launch {
            repo.history(chatId)
            repo.markRead(chatId)
        }
        repo.openChat(chatId)
    }

    /* ---------- отправка ---------- */
    fun sendMessage(text: String) {
        if (text.isNotBlank()) repo.send(text.trim())
    }

    /* ---------- подгрузка старых ---------- */
    fun loadMore() = viewModelScope.launch {
        val oldest = _messages.value.firstOrNull()?.id ?: return@launch
        repo.history(chatId, before = oldest)
    }

    override fun onCleared() {
        WsManager.close()
    }
}