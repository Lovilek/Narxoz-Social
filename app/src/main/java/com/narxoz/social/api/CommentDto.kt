package com.narxoz.social.api

import com.google.gson.annotations.SerializedName

data class CommentDto(
    val id: Int,

    /** id автора (пригодится, напр., для аватарки или перехода в профиль) */
    @SerializedName("author")         val authorId: Int,

    /** никнейм приходит отдельным полем  author_nickname  */
    @SerializedName("author_nickname")val authorNickname: String,

    val content: String,

    @SerializedName("created_at")     val createdAt: String
)