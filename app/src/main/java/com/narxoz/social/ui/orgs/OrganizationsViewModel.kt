package com.narxoz.social.ui.orgs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.api.dto.OrganizationDto
import com.narxoz.social.repository.OrganizationsRepository
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
    val avatarUrl: String?
)

class OrganizationsViewModel(
    private val repo: OrganizationsRepository = OrganizationsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(OrgsState(isLoading = true))
    val state: StateFlow<OrgsState> = _state

    init { reload() }

    fun reload() = viewModelScope.launch {
        _state.value = OrgsState(isLoading = true)
        repo.load()
            .onSuccess { dtos ->
                val mapped = dtos.map { dto ->
                    OrgUi(
                        id        = dto.id,
                        title     = dto.fullName,
                        subtitle  = "@${dto.nickname}",
                        avatarUrl = dto.avatarPath
                            ?.replace("127.0.0.1", "10.0.2.2")
                    )
                }
                _state.value = OrgsState(items = mapped)
            }
            .onFailure { e ->
                _state.value = OrgsState(error = e.message ?: "Ошибка загрузки")
            }
    }
}