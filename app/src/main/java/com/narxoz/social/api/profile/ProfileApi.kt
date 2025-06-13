package com.narxoz.social.api.profile

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

/** DTO профиля пользователя. */
data class UserProfileDto(
    val id: Int,
    @SerializedName("full_name") val fullName: String?,
    val nickname: String?,
    @SerializedName("avatar_path") val avatarPath: String?,
    val email: String?
)

interface ProfileApi {
    /** Возвращает профиль текущего пользователя. */
    @GET("api/users/profile/")
    suspend fun getProfile(): UserProfileDto
}