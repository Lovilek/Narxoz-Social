package com.narxoz.social.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            tokenProvider()?.let { header("Authorization", "Bearer $it") }
        }.build()
        return chain.proceed(request)
    }
}