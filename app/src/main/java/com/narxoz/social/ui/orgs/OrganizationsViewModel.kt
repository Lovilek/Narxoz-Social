package com.narxoz.social.ui.orgs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.api.dto.OrganizationDto
import com.narxoz.social.repository.OrganizationsRepository
import com.narxoz.social.repository.FriendsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrgsState(
    val items:     List<OrgUi> = emptyList(),
    val isLoading: Boolean     = false,
    val error:     String?     = null
)

data class OrgUi(
    val id: Int,
    val title: String,
    val subtitle: String,
    val avatarUrl: String?,
    val joined: Boolean = false,
)

class OrganizationsViewModel(
    private val repo: OrganizationsRepository = OrganizationsRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(OrgsState(isLoading = true))
    val state: StateFlow<OrgsState> = _state

    init { reload() }

    fun reload() = viewModelScope.launch {
        _state.value = OrgsState(isLoading = true)
        val orgsRes = repo.load()
        val friendsRes = friendsRepo.list()

        if (orgsRes.isSuccess) {
            val friendIds = friendsRes.getOrElse { emptyList() }.map { it.id }
            val mapped = orgsRes.getOrDefault(emptyList()).map { dto ->
                OrgUi(
                    id        = dto.id,
                    title     = dto.fullName,
                    subtitle  = "@${dto.nickname}",
                    avatarUrl = dto.avatarPath
                        ?.replace("127.0.0.1", "10.0.2.2"),
                    joined    = friendIds.contains(dto.id),
                )
            }
            _state.value = OrgsState(items = mapped)
        } else {
            _state.value = OrgsState(error = orgsRes.exceptionOrNull()?.message ?: "Ошибка загрузки")
        }
    }

    fun join(id: Int) = viewModelScope.launch {
        friendsRepo.send(id)
        reload()
    }

    fun leave(id: Int) = viewModelScope.launch {
        friendsRepo.remove(id)
        reload()
    }
}