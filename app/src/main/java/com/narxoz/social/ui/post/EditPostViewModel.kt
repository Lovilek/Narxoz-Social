package com.narxoz.social.ui.post

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditPostViewModel(
    private val postId: Int,
    private val repo: PostRepository = PostRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(EditPostState(isLoading = true))
    val state: StateFlow<EditPostState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        repo.get(postId)
            .onSuccess { dto ->
                _state.value = EditPostState(
                    content = dto.content,
                    images = dto.images.map { Uri.parse(it.imagePath.replace("127.0.0.1", "10.0.2.2")) }
                )
            }
            .onFailure { e ->
                _state.value = EditPostState(error = e.message ?: "Ошибка")
            }
    }

    fun setContent(text: String) { _state.update { it.copy(content = text) } }

    fun addImages(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        _state.update { it.copy(images = it.images + uris) }
    }

    fun save(onSaved: () -> Unit = {}) = viewModelScope.launch {
        val content = _state.value.content
        val files = _state.value.images.mapNotNull { runCatching { it.toFile() }.getOrNull() }
        _state.update { it.copy(isLoading = true, error = null) }
        repo.update(postId, content, files)
            .onSuccess {
                _state.update { st -> st.copy(isLoading = false) }
                onSaved()
            }
            .onFailure { e ->
                _state.update { st -> st.copy(isLoading = false, error = e.message ?: "Ошибка") }
            }
    }
}

class EditPostVmFactory(private val postId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EditPostViewModel(postId) as T
}
