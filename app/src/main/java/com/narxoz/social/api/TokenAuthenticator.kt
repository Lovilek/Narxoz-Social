package com.narxoz.social.api

import com.narxoz.social.repository.AuthRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {

        /* чтобы не уйти в бесконечный цикл */
        if (response.request.header("X-Token-Refreshed") != null) return null

        val newAccess = AuthRepository.refreshTokenBlocking()

        return if (newAccess != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .header("X-Token-Refreshed", "1")   // чтобы не зациклиться
                .build()
        } else null
    }
}