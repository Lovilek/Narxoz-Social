package com.narxoz.social.api.dto

import com.google.gson.annotations.SerializedName

data class OrganizationDto(
    val id: Int,

    @SerializedName("full_name")
    val fullName: String,

    val nickname: String,

    @SerializedName("avatar_path")
    val avatarPath: String?
)