package com.narxoz.social.ui.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LikesVmFactory(private val postId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LikesViewModel(postId) as T
}