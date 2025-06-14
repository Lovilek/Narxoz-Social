package com.narxoz.social.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.PostRepository
import com.narxoz.social.ui.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val postId: Int,
    private val repo: PostRepository = PostRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PostDetailState(isLoading = true))
    val state: StateFlow<PostDetailState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = PostDetailState(isLoading = true)
        repo.get(postId)
            .onSuccess { dto ->
                val post = Post(
                    id = dto.id.toInt(),
                    author = dto.author,
                    content = dto.content,
                    imageUrl = dto.images.firstOrNull()?.imagePath?.replace("127.0.0.1", "10.0.2.2"),
                    likes = dto.likes,
                    likedByMe = dto.isLiked
                )
                _state.value = PostDetailState(post = post)
            }
            .onFailure { e ->
                _state.value = PostDetailState(error = e.message ?: "Ошибка")
            }
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        repo.delete(postId)
            .onSuccess { onDone() }
            .onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Ошибка") }
            }
    }
}

class PostDetailVmFactory(private val postId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PostDetailViewModel(postId) as T
}
