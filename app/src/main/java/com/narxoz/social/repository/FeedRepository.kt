package com.narxoz.social.repository

import com.narxoz.social.api.*

class FeedRepository(private val api: FeedApi) {

    /* --- посты -------------------------------------------------------- */

    private var page = 1

    suspend fun loadPage(): Result<List<PostDto>> = runCatching {
        api.posts(page++).results          // PagedResponse → List<PostDto>
    }

    fun reset() { page = 1 }

    /* --- клубы (пока отсутствуют на сервере) -------------------------- */

    suspend fun getTopClubs():   Result<List<ClubDto>> = Result.success(emptyList())
    suspend fun getHorizClubs(): Result<List<ClubDto>> = Result.success(emptyList())
}