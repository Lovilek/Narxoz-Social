package com.narxoz.social.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.api.*
import com.narxoz.social.repository.AuthRepository
import com.narxoz.social.repository.FeedRepository
import com.narxoz.social.repository.LikesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class FeedState(
    val topClubs:        List<ClubDto> = emptyList(),
    val horizontalClubs: List<ClubDto> = emptyList(),
    val posts:           List<Post>    = emptyList(),
    val isLoading:       Boolean       = false,
    val error:           String?       = null
)

class FeedViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FeedRepository(RetrofitInstance.feedApi)

    private val _state = MutableStateFlow(FeedState(isLoading = true))
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var isLoadingMore = false

    init {
        initialLoad()
    }

    fun retry() = initialLoad()
    fun loadNext() {
        if (!isLoadingMore) appendNextPage()
    }

    /* ---------------- private ---------------- */

    private fun initialLoad() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repo.reset()
            _state.value = FeedState(isLoading = true)
            appendNextPage()
        }
    }

    private fun appendNextPage() = viewModelScope.launch {
        isLoadingMore = true
        runCatching {
            val pageDtos = repo.loadPage().getOrThrow()

            val mapped = pageDtos.map { dto ->
                Post(
                    id = dto.id.toInt(),
                    author = dto.author,
                    content = dto.content,
                    imageUrl = dto.images.firstOrNull()
                        ?.imagePath
                        ?.replace("127.0.0.1", "159.65.124.242"),
                    likes = dto.likes,
                    likedByMe = dto.isLiked
                )
            }

            _state.update {
                it.copy(
                    posts = it.posts + mapped,
                    isLoading = false,
                    error = null
                )
            }
        }.onFailure { e ->
            _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
        }
        isLoadingMore = false
    }

    private val likesRepo = LikesRepository()


    /* функция toggle */
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
        if (resp.isFailure) {            // откат при ошибке
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

            /* --- 1. скачиваем картинку, если есть --- */
            val imgUri: Uri? = post.imageUrl?.let { url ->
                try {
                    // берём Bitmap через Coil-ImageLoader
                    val loader = ImageLoader(ctx)
                    val req = ImageRequest.Builder(ctx)
                        .data(url.replace("127.0.0.1", "159.65.124.242"))
                        .allowHardware(false)
                        .build()
                    val drawable = (loader.execute(req).drawable) ?: return@let null
                    val bmp = (drawable as BitmapDrawable).bitmap

                    // сохраняем во временный файл
                    val cacheDir = File(ctx.cacheDir, "images").apply { mkdirs() }
                    val file = File(cacheDir, "post_${post.id}.jpg")
                    FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    // получаем uri через FileProvider
                    FileProvider.getUriForFile(
                        ctx,
                        "${ctx.packageName}.fileprovider",
                        file
                    )
                } catch (_: Exception) {
                    null
                }
            }

            /* --- 2. формируем Intent --- */
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

    fun refresh() = initialLoad()
}