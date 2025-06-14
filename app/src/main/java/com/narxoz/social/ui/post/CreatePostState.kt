package com.narxoz.social.ui.post

import android.net.Uri

data class CreatePostState(
    val content: String = "",
    val images: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)