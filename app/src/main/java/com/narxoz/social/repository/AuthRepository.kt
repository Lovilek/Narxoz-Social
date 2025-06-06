package com.narxoz.social.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.narxoz.social.api.AuthApi
import com.narxoz.social.api.LoginRequest
import com.narxoz.social.api.RefreshRequest
import com.narxoz.social.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {

    /* ---------- instance ---------- */
    private val localPrefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val api: AuthApi = RetrofitInstance.authApi   // ← только для login()

    /* ---------- companion ---------- */
    companion object {
        private const val KEY_ID = "user_id"
        private const val KEY_ACCESS  = "jwt_access"
        private const val KEY_REFRESH = "jwt_refresh"
        private const val KEY_ROLE    = "user_role"

        @Volatile private var cachedAccess:  String? = null
        @Volatile private var cachedRefresh: String? = null

        /** Возвращает id текущего пользователя, если сохранён, иначе null */
        fun getUserId(): Int? = RetrofitInstance.appPrefs
            .getString(KEY_ID, null)
            ?.toIntOrNull()

        /** Публичный геттер для Interceptor’а */
        fun getAccessToken(): String?  = cachedAccess
        fun getRefreshToken(): String? = cachedRefresh

        private val staticApi = RetrofitInstance.authApi
        private lateinit var appPrefs: SharedPreferences   // инициализируем один раз

        fun init(prefs: SharedPreferences) { appPrefs = prefs }

        private fun saveStaticTokens(acc: String, ref: String) {
            appPrefs.edit()
                .putString(KEY_ACCESS,  acc)
                .putString(KEY_REFRESH, ref)
                .apply()
            cachedAccess  = acc
            cachedRefresh = ref
        }

        fun refreshTokenBlocking(): String? {
            val refresh = cachedRefresh ?: return null
            return try {
                val resp = staticApi.refreshSync(RefreshRequest(refresh)).execute()
                if (resp.isSuccessful) {
                    val body = resp.body() ?: return null
                    saveStaticTokens(body.access, body.refresh)
                    body.access
                } else null
            } catch (_: Exception) { null }
        }

        fun forceLogout() {
            appPrefs.edit().clear().apply()
            cachedAccess = null
            cachedRefresh = null
            SessionManager.setLoggedOut()
        }

        private fun cacheTokens(acc: String?, ref: String?) {
            cachedAccess  = acc
            cachedRefresh = ref
        }

        /** Текущий access-токен — понадобится Interceptor’у */
        val currentToken: String?
            get() = cachedAccess
    }

    /* ---------- ctor ---------- */
    init {
        cacheTokens(
            localPrefs.getString(KEY_ACCESS,  null),
            localPrefs.getString(KEY_REFRESH, null)
        )
    }

    /* ---------- LOGIN ---------- */
    suspend fun login(username: String, password: String, staySignedIn: Boolean): String? =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.login(LoginRequest(username, password))

                if (resp.code() == 401) {          // access-токен просрочен
                    forceLogout()                  // очищаем кэш + SessionManager
                    return@withContext null        // UI покажет ошибку
                }
                /* ────────────────────────────── */

                if (resp.isSuccessful) {
                    val body = resp.body()!!

                    saveLocalTokens(body.access, body.refresh, staySignedIn)
                    saveRole(body.user.role, staySignedIn)
                    saveUserId(body.user.id,        staySignedIn)
                    return@withContext body.user.role
                }

                Log.e("AuthRepo", "login error ${resp.errorBody()?.string()}")
                null
            } catch (e: Exception) {
                Log.e("AuthRepo", "login network error ${e.message}")
                null
            }
        }

    /* ---------- LOGOUT ---------- */
    fun logout() = forceLogout()

    /* ---------- helpers ---------- */
    private fun saveLocalTokens(acc: String, ref: String, persist: Boolean) {
        if (persist) localPrefs.edit()
            .putString(KEY_ACCESS,  acc)
            .putString(KEY_REFRESH, ref)
            .apply()
        cacheTokens(acc, ref)
    }
    private fun saveRole(role: String, persist: Boolean) {
        if (persist) localPrefs.edit().putString(KEY_ROLE, role).apply()
    }

    /** Сохраняем ID пользователя сразу в оба SharedPreferences,
     *  чтобы getUserId() работал и до, и после перезапуска приложения */
    private fun saveUserId(id: Int, persist: Boolean) {
        if (persist) localPrefs.edit().putString(KEY_ID, id.toString()).apply()

        // appPrefs указывает на глобальные prefs, которые читает getUserId()
        RetrofitInstance.appPrefs
            .edit()
            .putString(KEY_ID, id.toString())
            .apply()
    }
}