package com.travel.app.data

import android.content.Context
import com.travel.app.BuildConfig
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.data.session.AuthInterceptor
import com.travel.app.data.session.SessionManager
import com.travel.app.domain.repository.UserRepository
import com.travel.app.service.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object AppContainer {
    private val BASE_URL = BuildConfig.BACKEND_URL

    lateinit var sessionManager: SessionManager
        private set

    fun initialize(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { sessionManager })
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    val userRepository: UserRepository = UserRepositoryImpl(apiService, { sessionManager })
}
