package com.travel.app.data

import android.content.Context
import com.travel.app.BuildConfig
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.data.repository.ActivityRepositoryImpl
import com.travel.app.data.repository.ItineraryRepositoryImpl
import com.travel.app.data.session.AuthInterceptor
import com.travel.app.data.session.SessionManager
import com.travel.app.domain.repository.UserRepository
import com.travel.app.domain.repository.ActivityRepository
import com.travel.app.domain.repository.ItineraryRepository
import com.travel.app.service.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        val str = reader.nextString()
        return if (str.isNullOrBlank()) null else LocalDateTime.parse(str, formatter)
    }
}

object AppContainer {
    private val BASE_URL = BuildConfig.BACKEND_URL

    lateinit var sessionManager: SessionManager
        private set

    val isInitialized: Boolean
        get() = ::sessionManager.isInitialized

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

    private val gson = com.google.gson.GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    val userRepository: UserRepository = UserRepositoryImpl(apiService, { sessionManager })
    val activityRepository: ActivityRepository = ActivityRepositoryImpl(apiService)
    val itineraryRepository: ItineraryRepository = ItineraryRepositoryImpl(apiService)
}
