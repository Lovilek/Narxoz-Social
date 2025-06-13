package com.narxoz.social.repository

import android.util.Log
import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.friends.FriendRespondRequest
import com.narxoz.social.api.friends.FriendsApi

class FriendsRepository(
    private val api: FriendsApi = RetrofitInstance.friendsApi
) {
    suspend fun send(id: Int) = runCatching { api.send(id) }
    suspend fun cancel(id: Int) = runCatching { api.cancel(id) }
    suspend fun remove(id: Int) = runCatching { api.remove(id) }
    suspend fun respond(id: Int, accepted: Boolean) =
        runCatching {
            val action = if (accepted) "accept" else "decline"
            api.respond(id, FriendRespondRequest(action))
        }.onFailure {
            Log.e("FriendsRepository", "Respond failed", it)
        }
    suspend fun outgoing() = runCatching { api.outgoing() }
    suspend fun declined() = runCatching { api.declined() }
    suspend fun status(id: Int) = runCatching { api.status(id) }
    suspend fun incoming() = runCatching { api.incoming() }
    suspend fun list() = runCatching { api.list() }
}