package com.narxoz.social

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.narxoz.social.repository.AuthRepository
import com.narxoz.social.api.RetrofitInstance

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        RetrofitInstance.appPrefs = prefs
        AuthRepository.init(prefs)
    }
}