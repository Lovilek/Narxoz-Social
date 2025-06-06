package com.narxoz.social.repository

import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.dto.OrganizationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrganizationsRepository {
    private val api = RetrofitInstance.orgApi

    suspend fun load(): Result<List<OrganizationDto>> =
        withContext(Dispatchers.IO) { runCatching { api.all() } }
}