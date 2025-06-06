package com.narxoz.social.api

import com.narxoz.social.api.CommentDto
import retrofit2.Response
import retrofit2.http.*

interface CommentsApi {

    @GET("api/posts/{post_id}/comments/")
    suspend fun getComments(
        @Path("post_id") postId: Int
    ): PageResponse<CommentDto>

    @POST("api/posts/{post_id}/comments/")
    suspend fun addComment(
        @Path("post_id") postId: Int,
        @Body body: NewCommentRequest
    ): CommentDto

    @DELETE("api/posts/{post_id}/comments/{comment_id}/")
    suspend fun deleteComment(
        @Path("post_id")    postId:    Int,
        @Path("comment_id") commentId: Int
    ): Response<Unit>
}