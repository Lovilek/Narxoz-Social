package com.narxoz.social.api

import com.google.gson.annotations.SerializedName

data class ClubDto(
    val id: Long,
    val title: String,
    @SerializedName("avatar_url")  val avatarUrl: String?,
    @SerializedName("banner_url")  val bannerUrl: String?
)

data class ImageDto(
    @SerializedName("image_path") val imagePath: String
)

data class PostDto(
    val id: Long,
    val author: String,
    val content: String,
    val images: List<ImageDto>,
    val likes: Int,
    @SerializedName("is_liked")
    val isLiked: Boolean
)

data class RefreshRequest(val refresh: String)

data class RefreshResponse(
    val access: String,
    val refresh: String
)

data class PagedPostsDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PostDto>
)