package com.narxoz.social.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class NotificationDto(
    val id: Int,
    val text: String,
    val isRead: Boolean
)

interface NotificationsApi {
    @GET("api/notifications/")
    suspend fun list(): List<NotificationDto>

    @POST("api/notifications/read/{id}/")
    suspend fun markRead(@Path("id") id: Int)

}