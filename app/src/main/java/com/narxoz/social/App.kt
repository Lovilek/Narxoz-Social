package com.narxoz.social

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.narxoz.social.repository.AuthRepository
import com.narxoz.social.api.RetrofitInstance
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.narxoz.social.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        RetrofitInstance.appPrefs = prefs
        AuthRepository.init(prefs)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = task.result ?: return@addOnCompleteListener
            NotificationRepository().apply {
                CoroutineScope(Dispatchers.IO).launch { sendToken(token) }
            }
        }
    }
}