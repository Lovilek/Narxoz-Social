package com.narxoz.social.repository

import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.likes.LikesApi

class LikesRepository(
    private val api: LikesApi = RetrofitInstance.likesApi
) {
    suspend fun toggle(postId: Int) = runCatching { api.toggle(postId) }
    suspend fun load (postId: Int)  = runCatching { api.list  (postId) }
}