package com.narxoz.social.api

import com.narxoz.social.api.dto.EventDto
import retrofit2.http.GET
import com.narxoz.social.api.PagedResponse
import retrofit2.http.Query

interface EventsApi {
    @GET("api/events/")
    suspend fun list(@Query("page") page: Int = 1): PagedResponse<EventDto>
}