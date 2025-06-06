package com.narxoz.social.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageDto(
    val id: String = "",

    @Json(name = "chat_id")
    val chatId: String = "",

    @Json(name = "sender_id")
    val sender: Int = 0,

    val text: String = "",

    @Json(name = "created_at")
    val createdAt: String? = null
)