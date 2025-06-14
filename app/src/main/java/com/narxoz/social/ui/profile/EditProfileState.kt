package com.narxoz.social.ui.profile

import android.net.Uri

/** Состояние экрана редактирования профиля. */
data class EditProfileState(
    val nickname: String = "",
    val avatarUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
