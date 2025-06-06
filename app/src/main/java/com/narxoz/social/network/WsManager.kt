package com.narxoz.social.network

import com.narxoz.social.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object WsManager {

    private var webSocket: WebSocket? = null
    private val okHttp = OkHttpClient()

    fun connect(chatId: String, jwt: String, listener: WebSocketListener) {
        val req = Request.Builder()
            .url("${BuildConfig.BASE_WS_URL}/ws/chat/$chatId/?token=$jwt")
            .build()
        webSocket = okHttp.newWebSocket(req, listener)
    }

    fun sendMessage(text: String) {
        val payload = """{"type":"send","text":"${text.trim()}"}"""
        webSocket?.send(payload)
    }

    fun close() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}