package com.narxoz.social.network

import com.narxoz.social.BuildConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response

object WsManager {

    private var webSocket: WebSocket? = null
    private val okHttp = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    private var lastChatId: String? = null
    private var lastJwt: String? = null
    private var lastListener: WebSocketListener? = null

    fun connect(chatId: String, jwt: String, listener: WebSocketListener) {
        lastChatId = chatId
        lastJwt = jwt
        lastListener = listener

        val req = Request.Builder()
            .url("${BuildConfig.BASE_WS_URL}ws/chat/$chatId/?token=$jwt")
            .build()
        webSocket = okHttp.newWebSocket(req, object : WebSocketListener() {
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                listener.onFailure(ws, t, response)
                scheduleReconnect()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                listener.onMessage(ws, text)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                listener.onClosing(ws, code, reason)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                listener.onClosed(ws, code, reason)
            }
        })
    }

    private fun scheduleReconnect() {
        val cid = lastChatId ?: return
        val token = lastJwt ?: return
        val lis = lastListener ?: return
        GlobalScope.launch {
            delay(2000)
            connect(cid, token, lis)
        }
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