package com.narxoz.social.ui.post

import android.net.Uri

data class EditPostState(
    val content: String = "",
    val images: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
