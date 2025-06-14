package com.narxoz.social.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.ProfileRepository
import com.narxoz.social.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnotherProfileViewModel(
    private val userId: Int,
    private val repo: ProfileRepository = ProfileRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(AnotherProfileState(isLoading = true))
    val state: StateFlow<AnotherProfileState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = AnotherProfileState(isLoading = true)
        val profileRes = repo.loadById(userId)
        val statusRes = friendsRepo.status(userId)
        _state.value = AnotherProfileState(
            profile = profileRes.getOrNull(),
            friendStatus = statusRes.getOrNull()?.status,
            isLoading = false,
            error = profileRes.exceptionOrNull()?.message ?: statusRes.exceptionOrNull()?.message
        )
    }
}

class AnotherProfileVmFactory(private val userId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AnotherProfileViewModel(userId) as T
}
