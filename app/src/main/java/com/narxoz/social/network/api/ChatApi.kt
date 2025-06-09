package com.narxoz.social.network.api

import retrofit2.Response
import com.narxoz.social.network.dto.ChatShortDto
import com.narxoz.social.network.dto.MessageDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

data class SendMessageRequest(val text: String)

data class GroupIdResponse(val group_id: String)

interface ChatApi {

    @GET("api/chats/allchats/")
    suspend fun getAllChats(): List<ChatShortDto>

    @GET("api/chats/{chatId}/messages/")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int = 30,
        @Query("before") beforeId: String? = null
    ): List<MessageDto>

    @POST("api/chats/{chatId}/read/")
    suspend fun markRead(@Path("chatId") chatId: String): Response<Unit>

    @POST("chats/{id}/messages")
    suspend fun sendMessage(
        @Path("id") chatId: String,
        @Body body: SendMessageRequest
    ): MessageDto

    @Multipart
    @POST("api/chats/group/create/")
    suspend fun createGroup(
        @Part("name") name: RequestBody,
        @Part members: List<MultipartBody.Part>,
        @Part avatar: MultipartBody.Part? = null
    ): GroupIdResponse
}