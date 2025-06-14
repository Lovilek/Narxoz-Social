package com.narxoz.social.repository

import com.narxoz.social.api.PostApi
import com.narxoz.social.api.PostDto
import com.narxoz.social.api.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class PostRepository(
    private val api: PostApi = RetrofitInstance.retrofit.create(PostApi::class.java)
) {
    suspend fun get(id: Int): Result<PostDto> =
        runCatching { api.getPost(id) }

    suspend fun create(content: String, images: List<File>): Result<PostDto> =
        runCatching {
            val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)
            val imageParts = images.map { file ->
                val req = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, req)
            }
            api.createPost(contentPart, imageParts)
        }

    suspend fun update(id: Int, content: String, images: List<File>): Result<PostDto> =
        runCatching {
            val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)
            val imageParts = images.map { file ->
                val req = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, req)
            }
            api.updatePost(id, contentPart, imageParts)
        }

    suspend fun delete(id: Int): Result<Unit> =
        runCatching {
            val resp = api.deletePost(id)
            if (!resp.isSuccessful) throw HttpException(resp)
        }
}