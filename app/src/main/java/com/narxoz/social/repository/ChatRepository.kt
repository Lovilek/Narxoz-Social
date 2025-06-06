package com.narxoz.social.repository

import com.narxoz.social.network.WsManager
import com.narxoz.social.network.api.ChatApi
import com.narxoz.social.network.dto.MessageDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ChatApi,
    private val tokenProvider: () -> String?
) {

    /* --------- сообщения открытого диалога ------- */
    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    /* --------- событие «пришло новое сообщение» --- */
    private val _incoming = MutableSharedFlow<MessageDto>(extraBufferCapacity = 64)
    val incoming: SharedFlow<MessageDto> = _incoming.asSharedFlow()

    /* --------- событие «чат прочитан» ------------- */
    private val _read = MutableSharedFlow<String>(extraBufferCapacity = 32) // chatId
    val read: SharedFlow<String> = _read.asSharedFlow()

    /* --------- WebSocket -------------------------- */
    fun openChat(chatId: String) {
        WsManager.connect(
            chatId,
            tokenProvider() ?: "",
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    val moshi   = Moshi.Builder().build()
                    val adapter = moshi.adapter(MessageDto::class.java)
                    val msg     = adapter.fromJson(text) ?: return

                    /* 1. В список сообщений текущего чата */
                    _messages.update { it + msg }

                    /* 2. Широковещательные события */
                    GlobalScope.launch { _incoming.emit(msg) }   // оптимистично, без подвисания
                }
            }
        )
    }

    /* --------- REST ------------------------------- */
    fun send(text: String) = WsManager.sendMessage(text)

    suspend fun history(chatId: String, before: String? = null) {
        val batch = api.getMessages(chatId, beforeId = before)
        if (before == null)  _messages.value  = batch
        else                  _messages.update { it + batch }
    }

    suspend fun markRead(chatId: String) {
        api.markRead(chatId)             // → бэкенд сбрасывает счётчик
        _read.emit(chatId)               // → мгновенный отклик в UI
    }
}