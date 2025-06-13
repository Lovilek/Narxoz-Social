package com.narxoz.social.ui.profile

import com.narxoz.social.api.profile.UserProfileDto

/** Состояние экрана профиля. */
data class ProfileState(
    val profile: UserProfileDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)