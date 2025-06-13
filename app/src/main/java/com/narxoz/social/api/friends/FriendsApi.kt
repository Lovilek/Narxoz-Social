package com.narxoz.social.api.friends

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * DTO для краткого представления пользователя.
 */
data class UserShortDto(
    val id: Int,
    val nickname: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("avatar_path") val avatarPath: String?
)

/** Запрос на ответ на входящую заявку */
data class FriendRespondRequest(val action: String)

/** Заявка в друзья */
data class FriendRequestDto(
    val id: Int,
    @SerializedName("from_user") val fromUser: UserShortDto?,
    @SerializedName("to_user") val toUser: UserShortDto?,
    val status: String?
)

/** Статус дружбы */
data class FriendStatusDto(val status: String)

interface FriendsApi {
    @POST("api/friends/send/{id}/")
    suspend fun send(@Path("id") id: Int)

    @DELETE("api/friends/cancel/{id}/")
    suspend fun cancel(@Path("id") id: Int)

    @DELETE("api/friends/remove/{id}/")
    suspend fun remove(@Path("id") id: Int)

    @POST("api/friends/respond/{id}/")
    suspend fun respond(
        @Path("id") id: Int,
        @Body body: FriendRespondRequest
    )

    @GET("api/friends/outgoing/")
    suspend fun outgoing(): List<FriendRequestDto>

    @GET("api/friends/declined/")
    suspend fun declined(): List<FriendRequestDto>

    @GET("api/friends/status/{id}/")
    suspend fun status(@Path("id") id: Int): FriendStatusDto

    @GET("api/friends/incoming/")
    suspend fun incoming(): List<FriendRequestDto>

    @GET("api/friends/list/")
    suspend fun list(): List<UserShortDto>
}