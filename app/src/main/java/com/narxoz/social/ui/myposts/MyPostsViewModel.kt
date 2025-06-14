package com.narxoz.social.ui.myposts

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.narxoz.social.repository.LikesRepository
import com.narxoz.social.repository.PostRepository
import com.narxoz.social.ui.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MyPostsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PostRepository()
    private val likesRepo = LikesRepository()

    private val _state = MutableStateFlow(MyPostsState(isLoading = true))
    val state: StateFlow<MyPostsState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = MyPostsState(isLoading = true)
        repo.myPosts()
            .onSuccess { dtos ->
                val posts = dtos.map { dto ->
                    Post(
                        id = dto.id.toInt(),
                        author = dto.author,
                        content = dto.content,
                        imageUrl = dto.images.firstOrNull()?.imagePath?.replace("127.0.0.1", "10.0.2.2"),
                        likes = dto.likes,
                        likedByMe = dto.isLiked
                    )
                }
                _state.value = MyPostsState(posts = posts)
            }
            .onFailure { e ->
                _state.value = MyPostsState(error = e.message ?: "Ошибка")
            }
    }

    fun toggleLike(postId: Int) = viewModelScope.launch {
        _state.update { s ->
            s.copy(posts = s.posts.map {
                if (it.id == postId)
                    it.copy(
                        likedByMe = !it.likedByMe,
                        likes = it.likes + if (it.likedByMe) -1 else +1
                    )
                else it
            })
        }
        val resp = likesRepo.toggle(postId)
        if (resp.isFailure) {
            _state.update { s ->
                s.copy(posts = s.posts.map {
                    if (it.id == postId)
                        it.copy(
                            likedByMe = !it.likedByMe,
                            likes = it.likes + if (it.likedByMe) -1 else +1
                        )
                    else it
                })
            }
        }
    }

    fun share(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = getApplication<Application>().applicationContext

            val imgUri: Uri? = post.imageUrl?.let { url ->
                try {
                    val loader = ImageLoader(ctx)
                    val req = ImageRequest.Builder(ctx)
                        .data(url.replace("127.0.0.1", "10.0.2.2"))
                        .allowHardware(false)
                        .build()
                    val drawable = (loader.execute(req).drawable) ?: return@let null
                    val bmp = (drawable as BitmapDrawable).bitmap
                    val cacheDir = File(ctx.cacheDir, "images").apply { mkdirs() }
                    val file = File(cacheDir, "post_${post.id}.jpg")
                    FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    FileProvider.getUriForFile(
                        ctx,
                        "${ctx.packageName}.fileprovider",
                        file
                    )
                } catch (_: Exception) {
                    null
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                if (imgUri != null) {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imgUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
                putExtra(
                    Intent.EXTRA_TEXT,
                    buildString {
                        append("${post.author}:\n${post.content}")
                        if (imgUri == null && post.imageUrl != null)
                            append("\n\n${post.imageUrl}")
                    }
                )
            }

            val chooser = Intent.createChooser(intent, "Поделиться постом")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            withContext(Dispatchers.Main) { ctx.startActivity(chooser) }
        }
    }
}