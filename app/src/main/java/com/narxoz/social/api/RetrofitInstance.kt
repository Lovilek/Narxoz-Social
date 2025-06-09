package com.narxoz.social.api

import android.content.SharedPreferences
import com.narxoz.social.api.likes.LikesApi
import com.narxoz.social.api.EventsApi
import com.narxoz.social.repository.AuthRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:8000/"
    lateinit var appPrefs: SharedPreferences

    /** Подставляем токен, кроме login/refresh/register */
    private val authInterceptor = Interceptor { chain ->
        val req = chain.request()
        val path = req.url.encodedPath

        val needAuth = !path.endsWith("/login/") &&
                !path.endsWith("/register/") &&
                !path.endsWith("/token/refresh/")

        val access = AuthRepository.getAccessToken()
        val newReq = if (needAuth && access != null)
            req.newBuilder()
                .addHeader("Authorization", "Bearer $access")
                .build()
        else req

        chain.proceed(newReq)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .authenticator(TokenAuthenticator())   // ← автоматический refresh
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttp)
        .build()

    val authApi: AuthApi  by lazy { retrofit.create(AuthApi ::class.java) }
    val feedApi: FeedApi  by lazy { retrofit.create(FeedApi ::class.java) }
    val commentsApi: CommentsApi by lazy {
        retrofit.create(CommentsApi::class.java)
    }
    val likesApi: LikesApi by lazy { retrofit.create(LikesApi::class.java) }
    val orgApi: OrganizationsApi by lazy {
        retrofit.create(OrganizationsApi::class.java)
    }
    val eventsApi: EventsApi by lazy {
        retrofit.create(EventsApi::class.java)
    }
}