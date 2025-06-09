package com.narxoz.social.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.FcmTokenRequest
import com.narxoz.social.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "New token: $token")
        sendToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCMService", "Message: ${'$'}{message.data}")
        // TODO: показать уведомление пользователю
    }

    private fun sendToken(token: String) {
        val access = AuthRepository.getAccessToken() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitInstance.notificationsApi
                    .registerToken(FcmTokenRequest(token))
            } catch (e: Exception) {
                Log.e("FCMService", "token send error", e)
            }
        }
    }
}
