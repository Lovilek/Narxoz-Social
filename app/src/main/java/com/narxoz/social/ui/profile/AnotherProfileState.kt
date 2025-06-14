package com.narxoz.social.ui.profile

import com.narxoz.social.api.profile.AnotherUserProfileDto

/** Состояние экрана чужого профиля. */
data class AnotherProfileState(
    val profile: AnotherUserProfileDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)