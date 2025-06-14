package com.narxoz.social.api.profile

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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

    /** Обновить профиль пользователя (nickname и аватар). */
    @Multipart
    @POST("api/users/update/")
    suspend fun updateProfile(
        @Part("nickname") nickname: RequestBody?,
        @Part avatar: MultipartBody.Part? = null
    ): UserProfileDto
}