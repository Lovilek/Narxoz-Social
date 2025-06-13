package com.narxoz.social.repository

import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.profile.ProfileApi
import com.narxoz.social.api.profile.UserProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val api: ProfileApi = RetrofitInstance.profileApi
) {
    suspend fun load(): Result<UserProfileDto> = withContext(Dispatchers.IO) {
        runCatching { api.getProfile() }
    }
}
