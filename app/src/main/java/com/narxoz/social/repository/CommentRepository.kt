package com.narxoz.social.repository

import com.narxoz.social.api.*
import com.narxoz.social.api.CommentDto
import retrofit2.HttpException

class CommentsRepository(
    private val api: CommentsApi = RetrofitInstance.commentsApi
) {

    suspend fun load(postId: Int): Result<List<CommentDto>> =
        runCatching { api.getComments(postId).results }

    suspend fun add(postId: Int, text: String): Result<CommentDto> =
        runCatching { api.addComment(postId, NewCommentRequest(text)) }

    suspend fun delete(postId: Int, commentId: Int): Result<Unit> =
        runCatching {
            val resp = api.deleteComment(postId, commentId)
            if (!resp.isSuccessful)   // 4xx / 5xx
                throw HttpException(resp)
        }
}