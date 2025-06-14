package com.narxoz.social.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostApi {
    @GET("api/posts/{id}/")
    suspend fun getPost(@Path("id") id: Int): PostDto

    @Multipart
    @POST("api/posts/")
    suspend fun createPost(
        @Part("content") content: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): PostDto

    @Multipart
    @PATCH("api/posts/{id}/")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Part("content") content: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): PostDto

    @DELETE("api/posts/{id}/")
    suspend fun deletePost(@Path("id") id: Int): Response<Unit>
}