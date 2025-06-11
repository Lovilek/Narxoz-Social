package com.narxoz.social.repository

import com.narxoz.social.api.NotificationDto
import com.narxoz.social.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository {
    private val api = RetrofitInstance.notificationsApi

    suspend fun list(): Result<List<NotificationDto>> = withContext(Dispatchers.IO) {
        runCatching { api.list() }
    }

    suspend fun markRead(id: Int) = withContext(Dispatchers.IO) {
        runCatching { api.markRead(id) }
    }
}