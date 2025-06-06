package com.narxoz.social.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Позволяет передать postId в CommentsViewModel при создании через viewModel() */
class CommentsVmFactory(private val postId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CommentsViewModel(postId) as T
}