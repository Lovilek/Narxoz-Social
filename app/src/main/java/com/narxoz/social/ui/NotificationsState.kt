package com.narxoz.social.ui

import com.narxoz.social.api.NotificationDto

data class NotificationsState(
    val list: List<NotificationDto> = emptyList(),
    val unread: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
