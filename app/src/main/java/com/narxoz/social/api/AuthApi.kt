package com.narxoz.social.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Модель данных для входа
data class LoginRequest(val login: String, val password: String)

data class LoginResponse(
    val access: String,
    val refresh: String,
    val user: UserResponse
)

data class UserResponse(
    @SerializedName("id")   val id:   Int,
    @SerializedName("role") val role: String
)


// Интерфейс API
interface AuthApi {
    @POST("api/users/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/users/token/refresh/")
    suspend fun refresh(@Body req: RefreshRequest): Response<RefreshResponse>

    @POST("api/users/token/refresh/")
    fun refreshSync(@Body req: RefreshRequest): Call<RefreshResponse>

    @POST("api/users/accept-policy/")
    suspend fun acceptPolicy(): Response<MessageResponse>

}