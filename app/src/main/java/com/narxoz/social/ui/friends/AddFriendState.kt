package com.narxoz.social.ui.friends

/** Состояние экрана отправки заявки в друзья */
data class AddFriendState(
    val idInput: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)