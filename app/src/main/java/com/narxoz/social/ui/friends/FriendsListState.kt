package com.narxoz.social.ui.friends

import com.narxoz.social.api.friends.UserShortDto

/** Состояние экрана со списком друзей */
data class FriendsListState(
    val friends: List<UserShortDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
