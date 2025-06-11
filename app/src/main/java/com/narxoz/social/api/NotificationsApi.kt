package com.narxoz.social.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

import com.narxoz.social.api.PagedResponse

data class NotificationDto(
    val id: Int,
    @SerializedName("text")     val text: String?,
    @SerializedName("is_read")  val isRead: Boolean
)

interface NotificationsApi {
    @GET("api/notifications/")
    suspend fun list(): PagedResponse<NotificationDto>

    @POST("api/notifications/read/{id}/")
    suspend fun markRead(@Path("id") id: Int)

}