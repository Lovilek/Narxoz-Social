package com.narxoz.social.ui.notifications

import com.narxoz.social.api.NotificationDto

data class NotificationsState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)