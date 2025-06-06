package com.narxoz.social.ui

data class Club(
    val title: String,
    val avatarRes: Int = 0,
    val bannerRes: Int = 0
)

data class Post(
    val id: Int,
    val author: String,
    val content: String,
    val imageUrl: String?,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shareLink: String? = null
)