package com.narxoz.social.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

import com.narxoz.social.api.PagedResponse

data class NotificationDto(
    val id: Int,
    val type: String?,
    val data: NotificationData?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("is_read") val isRead: Boolean
) {
    val text: String
        get() = when (type) {
            "event_reminder" -> data?.event?.title ?: ""
            "friend_request" -> "Friend request from ${data?.friend?.nickname ?: ""}"
            else -> data?.toString() ?: ""
        }
}

data class NotificationData(
    val type: String?,
    val event: EventBrief?,
    val friend: FriendBrief?,
)

data class EventBrief(
    val id: Int,
    val title: String?
)

data class FriendBrief(
    val id: Int,
    val nickname: String?,
)

interface NotificationsApi {
    @GET("api/notifications/")
    suspend fun list(): PagedResponse<NotificationDto>

    @POST("api/notifications/read/{id}/")
    suspend fun markRead(@Path("id") id: Int)

}