package com.narxoz.social.api

import com.narxoz.social.api.dto.EventDto
import retrofit2.http.GET

interface EventsApi {
    @GET("api/events/")
    suspend fun all(): List<EventDto>
}
