package com.narxoz.social.api.likes

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

import com.google.gson.annotations.SerializedName
import com.narxoz.social.api.PageResponse

data class LikeDto(
    val id: Long,
    val author: Int,
    @SerializedName("author_nickname")
    val authorNickname: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class ToggleLikeResponse(val status: String)

interface LikesApi {
    @POST("api/posts/{post_id}/like/")
    suspend fun toggle(
        @Path("post_id") postId: Int
    ): ToggleLikeResponse            // "ok" | "unliked"

    @GET("api/posts/{post_id}/likes/")
    suspend fun list(
        @Path("post_id") postId: Int
    ): PageResponse<LikeDto>
}