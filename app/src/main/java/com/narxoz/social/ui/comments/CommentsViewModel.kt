package com.narxoz.social.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.api.CommentsApi
import com.narxoz.social.api.RetrofitInstance
import com.narxoz.social.api.CommentDto
import com.narxoz.social.repository.CommentsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/* ---------- VM ---------- */
class CommentsViewModel(
    private val postId: Int,
    private val repo: CommentsRepository = CommentsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(CommentsState(isLoading = true))
    val  state:  StateFlow<CommentsState> = _state.asStateFlow()

    /** Публичный flow только с самим списком */
    val comments: StateFlow<List<CommentDto>> =
        _state.map { it.comments }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { reload() }

    /** Получаем список с сервера */
    fun reload() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        repo.load(postId)
            .onSuccess { list ->
                _state.update { s -> s.copy(isLoading = false, comments = list) }
            }
            .onFailure { e ->
                _state.update { s ->
                    s.copy(isLoading = false, error = e.message ?: "Ошибка сети")
                }
            }
    }

    /* Функция send() */
    fun send(text: String) = viewModelScope.launch {
        if (text.isBlank()) return@launch

        repo.add(postId, text.trim())
            .onSuccess { newCmt ->
                _state.update { s ->
                    s.copy(
                        comments = listOf(newCmt) + s.comments,
                        error = null              // очищаем старую ошибку
                    )
                }
            }
            .onFailure {
                _state.update { s ->
                    s.copy(error = "Не удалось отправить комментарий")
                }
            }
    }

    fun delete(commentId: Int) = viewModelScope.launch {
        repo.delete(postId, commentId)
            .onSuccess {
                _state.update { s ->
                    s.copy(comments = s.comments.filterNot { it.id == commentId })
                }
            }
            .onFailure { e ->
                _state.update { s -> s.copy(error = e.message ?: "Ошибка удаления") }
            }
    }
}