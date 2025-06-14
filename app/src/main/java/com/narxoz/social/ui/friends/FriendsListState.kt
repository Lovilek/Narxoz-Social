package com.narxoz.social.ui.friends

import com.narxoz.social.api.friends.FriendRequestDto
import com.narxoz.social.api.friends.UserShortDto

/** Tab categories on friends screen */
enum class FriendsTab { FRIENDS, INCOMING, OUTGOING }

/** Состояние экрана со списком друзей и заявок */
data class FriendsListState(
    val friends: List<UserShortDto> = emptyList(),
    val incoming: List<FriendRequestDto> = emptyList(),
    val outgoing: List<FriendRequestDto> = emptyList(),
    val tab: FriendsTab = FriendsTab.FRIENDS,
    val filter: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)