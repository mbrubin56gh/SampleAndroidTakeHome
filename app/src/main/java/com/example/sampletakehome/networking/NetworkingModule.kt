package com.example.sampletakehome.networking

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
object NetworkingModule {
    @Provides
    fun providesMoshi(): Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Provides
    fun providesUsersRetrofitBuilder(moshi: Moshi): Retrofit.Builder = Retrofit.Builder()
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(MoshiConverterFactory.create(moshi))

    @Provides
    fun providesUsersRetrofit(builder: Retrofit.Builder): Retrofit = builder
        .baseUrl("https://dummyjson.com/")
        .build()

    @Provides
    fun providesUsersService(usersRetrofit: Retrofit): UsersService {
        return usersRetrofit.create(UsersService::class.java)
    }
}