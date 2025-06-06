package com.narxoz.social.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private val _isLoggedOut = MutableStateFlow(false)
    val   isLoggedOut: StateFlow<Boolean> = _isLoggedOut
    internal fun setLoggedOut() { _isLoggedOut.value = true }
}