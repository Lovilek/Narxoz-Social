package com.narxoz.social.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


/** API for working with posts. */

interface PostApi {
    @GET("api/posts/{id}/")
    suspend fun getPost(@Path("id") id: Int): PostDto

    @POST("api/posts/create/")
    suspend fun createPost(@Body body: Map<String, String>): PostDto

    @PATCH("api/posts/{id}/")
    suspend fun updatePost(@Path("id") id: Int, @Body body: Map<String, String>): PostDto

    @Multipart
    @POST("api/posts/image-upload/{id}/")
    suspend fun uploadImage(
        @Path("id") id: Int,
        @Part image: MultipartBody.Part
    ): ImageDto

    @DELETE("api/posts/{id}/")
    suspend fun deletePost(@Path("id") id: Int): Response<Unit>

    /**
     * Returns the current user's posts. The backend wraps the list
     * of posts into a paginated response, so we return [PagedPostsDto].
     */
    @GET("api/posts/user/")
    suspend fun myPosts(): PagedPostsDto
}
