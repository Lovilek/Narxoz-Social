package com.narxoz.social.repository

import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.profile.ProfileApi
import com.narxoz.social.api.profile.UserProfileDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProfileRepository(
    private val api: ProfileApi = RetrofitInstance.profileApi
) {
    suspend fun load(): Result<UserProfileDto> = withContext(Dispatchers.IO) {
        runCatching { api.getProfile() }
    }

    suspend fun update(nickname: String?, avatar: File?): Result<UserProfileDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val nickPart = nickname?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }
                val avatarPart = avatar?.let { file ->
                    val req = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("avatar", file.name, req)
                }
                api.updateProfile(nickPart, avatarPart)
            }
        }
}