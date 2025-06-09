package com.narxoz.social.repository

import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.dto.EventDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository {
    private val api = RetrofitInstance.eventsApi

    suspend fun load(): Result<List<EventDto>> =
        withContext(Dispatchers.IO) { runCatching { api.all() } }
}