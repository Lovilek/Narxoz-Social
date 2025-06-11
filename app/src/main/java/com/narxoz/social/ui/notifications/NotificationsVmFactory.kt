package com.narxoz.social.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NotificationsVmFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        NotificationsViewModel() as T
}