package com.narxoz.social.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FeedApi {

    /* посты (страницы) */
    @GET("api/posts/")
    suspend fun posts(@Query("page") page: Int = 1): PagedResponse<PostDto>
}