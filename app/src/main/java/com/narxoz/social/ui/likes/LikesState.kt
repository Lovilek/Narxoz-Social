package com.narxoz.social.ui.likes

import com.narxoz.social.api.likes.LikeDto

data class LikesState(
    val likes: List<LikeDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)