package com.narxoz.social.ui.myposts

import com.narxoz.social.ui.Post

data class MyPostsState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)