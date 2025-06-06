package com.narxoz.social.api

import com.narxoz.social.api.dto.OrganizationDto
import retrofit2.http.GET

interface OrganizationsApi {
    @GET("api/users/organizations/")
    suspend fun all(): List<OrganizationDto>
}