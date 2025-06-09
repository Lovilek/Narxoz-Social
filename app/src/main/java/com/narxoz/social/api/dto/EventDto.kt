package com.narxoz.social.api.dto

import com.google.gson.annotations.SerializedName

data class EventDto(
    val id: Int,
    val title: String,
    @SerializedName("starts_at") val startsAt: String,
    val description: String?
)
