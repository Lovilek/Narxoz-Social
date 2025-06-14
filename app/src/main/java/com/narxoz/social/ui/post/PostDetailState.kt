package com.narxoz.social.ui.post

import com.narxoz.social.ui.Post

data class PostDetailState(
    val post: Post? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)