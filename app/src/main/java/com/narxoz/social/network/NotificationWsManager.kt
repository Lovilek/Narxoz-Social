package com.narxoz.social.network

import com.narxoz.social.BuildConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.math.pow

object NotificationWsManager {
    private var webSocket: WebSocket? = null
    private val okHttp = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    private var lastJwt: String? = null
    private var lastListener: WebSocketListener? = null
    private var reconnectAttempts = 0

    fun connect(jwt: String, listener: WebSocketListener) {
        lastJwt = jwt
        lastListener = listener
        val req = Request.Builder()
            .url("${BuildConfig.BASE_WS_URL}/ws/notify/?token=$jwt")
            .build()
        webSocket = okHttp.newWebSocket(req, object : WebSocketListener() {
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                listener.onFailure(ws, t, response)
                scheduleReconnect()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                reconnectAttempts = 0
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
        val token = lastJwt ?: return
        val lis = lastListener ?: return
        val delayMs = (2000.0 * 2.0.pow(reconnectAttempts.toDouble())).toLong().coerceAtMost(60_000)
        GlobalScope.launch {
            delay(delayMs)
            reconnectAttempts++
            connect(token, lis)
        }
    }

    fun close() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}
