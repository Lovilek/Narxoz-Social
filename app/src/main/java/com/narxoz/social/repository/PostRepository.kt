package com.narxoz.social.repository

import com.narxoz.social.api.PostApi
import com.narxoz.social.api.PostDto
import com.narxoz.social.api.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class PostRepository(
    private val api: PostApi = RetrofitInstance.postApi
) {
    suspend fun get(id: Int): Result<PostDto> =
        runCatching { api.getPost(id) }

    suspend fun create(content: String, images: List<File>): Result<PostDto> =
        runCatching {
            val post = api.createPost(mapOf("content" to content))
            images.forEach { file ->
                val req = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image_path", file.name, req)
                api.uploadImage(post.id.toInt(), part)
            }
            api.getPost(post.id.toInt())
        }

    suspend fun update(id: Int, content: String, images: List<File>): Result<PostDto> =
        runCatching {
            api.updatePost(id, mapOf("content" to content))
            images.forEach { file ->
                val req = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image_path", file.name, req)
                api.uploadImage(id, part)
            }
            api.getPost(id)
        }

    suspend fun delete(id: Int): Result<Unit> =
        runCatching {
            val resp = api.deletePost(id)
            if (!resp.isSuccessful) throw HttpException(resp)
        }

    suspend fun myPosts(): Result<List<PostDto>> =
        runCatching { api.myPosts().results }
}
