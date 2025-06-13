package com.narxoz.social.api

import com.google.gson.annotations.SerializedName
import com.narxoz.social.api.friends.FriendRequestDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * DTO одного уведомления.
 *
 * Для friend_request в поле [data.request] приходит полный объект FriendRequestDto,
 * откуда берём requestId, имя отправителя и т. д.
 */
data class NotificationDto(
    val id: Int,
    val type: String,
    val data: NotificationPayload?,            // event_reminder | friend_request | …
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("is_read") val isRead: Boolean,
) {
    /** Человекочитаемый текст для списка уведомлений */
    val text: String
        get() = when (type) {
            "event_reminder" -> data?.event?.title ?: ""
            "friend_request" -> {
                val u = data?.request?.fromUser
                "Friend request from ${u?.fullName ?: u?.nickname ?: ""}"
            }
            else -> ""
        }

    /** Удобный аксессор: ID самой заявки (FriendRequest.id) */
    val requestId: Int?
        get() = data?.request?.id
}

/**
 * Содержимое поля `data` в ответе backend.
 *
 * * event_reminder → заполнено только [event]
 * * friend_request → заполнено только [request]
 */
data class NotificationPayload(
    val event: EventBrief? = null,
    val request: FriendRequestDto? = null,
)

/** Упрощённый объект события для напоминаний */
data class EventBrief(
    val id: Int,
    val title: String?,
)

/* -------------------------------------------------------------------------- */
/*                                   API                                      */
/* -------------------------------------------------------------------------- */

interface NotificationsApi {

    /** Список уведомлений (постранично) */
    @GET("api/notifications/")
    suspend fun list(): PagedResponse<NotificationDto>

    /** Пометить уведомление прочитанным */
    @POST("api/notifications/read/{id}/")
    suspend fun markRead(@Path("id") id: Int)
}