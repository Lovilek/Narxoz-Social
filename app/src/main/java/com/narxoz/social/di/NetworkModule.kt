package com.narxoz.social.di

import com.narxoz.social.network.AuthInterceptor
import com.narxoz.social.network.api.ChatApi
import com.narxoz.social.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** 👉 здесь мы просто прокидываем функцию, которая возвращает текущий JWT (или null) */
    @Provides
    fun provideOkHttp(tokenProvider: () -> String?): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })   // вызов лямбды!
            .build()

    @Provides @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl("http://159.65.124.242:8000/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttp)
            .build()
    }

    @Provides
    fun provideChatApi(retrofit: Retrofit): ChatApi =
        retrofit.create(ChatApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {

    @Provides
    fun provideJwtLambda(): () -> String? =
        { AuthRepository.getAccessToken() }
}