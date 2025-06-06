package com.narxoz.social.ui.comments

import com.narxoz.social.api.CommentDto

data class CommentsState(
    val comments:  List<CommentDto> = emptyList(),
    val isLoading: Boolean          = false,
    val error:     String?          = null
)