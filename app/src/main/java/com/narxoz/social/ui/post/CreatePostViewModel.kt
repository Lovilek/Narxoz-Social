package com.narxoz.social.ui.post

import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val repo: PostRepository = PostRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    fun setContent(text: String) { _state.update { it.copy(content = text) } }

    fun addImages(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        _state.update { it.copy(images = it.images + uris) }
    }

    fun create(onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val content = _state.value.content
        val images = _state.value.images.mapNotNull { runCatching { it.toFile() }.getOrNull() }
        _state.update { it.copy(isLoading = true, error = null, success = false) }
        repo.create(content, images)
            .onSuccess {
                _state.update { st -> st.copy(isLoading = false, success = true) }
                onSuccess()
            }
            .onFailure { e ->
                _state.update { st -> st.copy(isLoading = false, error = e.message ?: "Ошибка") }
            }
    }
}