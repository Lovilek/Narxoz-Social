package com.narxoz.social.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatShortDto(
    val id: String,
    val type: String,
    val name: String?,                        // null для direct-чатов
    @Json(name = "avatar_url")
    val avatarUrl: String?,
    val unread: Int,

    @Json(name = "last_message")
    val lastMessage: MessageDto? = null       // nullable + дефолт
)