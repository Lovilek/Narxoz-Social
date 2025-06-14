package com.narxoz.social.ui.orgs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.ProfileRepository
import com.narxoz.social.repository.FriendsRepository
import com.narxoz.social.ui.profile.AnotherProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrganizationDetailViewModel(
    private val orgId: Int,
    private val profileRepo: ProfileRepository = ProfileRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(AnotherProfileState(isLoading = true))
    val state: StateFlow<AnotherProfileState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = AnotherProfileState(isLoading = true)
        val profileRes = profileRepo.loadById(orgId)
        val statusRes = friendsRepo.status(orgId)
        _state.value = AnotherProfileState(
            profile = profileRes.getOrNull(),
            friendStatus = statusRes.getOrNull()?.status,
            isLoading = false,
            error = profileRes.exceptionOrNull()?.message ?: statusRes.exceptionOrNull()?.message
        )
    }

    fun join() = viewModelScope.launch {
        friendsRepo.send(orgId)
        load()
    }

    fun leave() = viewModelScope.launch {
        friendsRepo.remove(orgId)
        load()
    }
}

class OrganizationDetailVmFactory(private val orgId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OrganizationDetailViewModel(orgId) as T
}