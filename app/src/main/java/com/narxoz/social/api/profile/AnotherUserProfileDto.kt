package com.narxoz.social.api.profile

import com.google.gson.annotations.SerializedName
import com.narxoz.social.api.friends.UserShortDto

/** DTO профиля другого пользователя. */
data class AnotherUserProfileDto(
    val id: Int,
    @SerializedName("full_name") val fullName: String?,
    val nickname: String?,
    @SerializedName("avatar_path") val avatarPath: String?,
    val friends: List<UserShortDto>?,
    @SerializedName("last_seen") val lastSeen: String?,
    @SerializedName("is_online") val isOnline: Boolean?
)