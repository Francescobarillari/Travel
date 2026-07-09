package com.travel.app.data

import android.content.Context
import com.travel.app.BuildConfig
import com.travel.app.data.repository.UserRepositoryImpl
import com.travel.app.data.repository.ActivityRepositoryImpl
import com.travel.app.data.repository.ItineraryRepositoryImpl
import com.travel.app.data.repository.LocalitaRepositoryImpl
import com.travel.app.data.remote.ReviewApiService
import com.travel.app.data.session.AuthInterceptor
import com.travel.app.data.session.SessionManager
import com.travel.app.data.session.TokenAuthenticator
import com.travel.app.domain.repository.UserRepository
import com.travel.app.domain.repository.ActivityRepository
import com.travel.app.domain.repository.ItineraryRepository
import com.travel.app.domain.repository.LocalitaRepository
import com.travel.app.domain.repository.ReviewRepository
import com.travel.app.service.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val str = reader.nextString()
        return if (str.isNullOrBlank()) null else LocalDateTime.parse(str, formatter)
    }
}

// Senza questi adapter Gson serializza LocalDate/LocalTime come oggetti
// ({"year":...,"month":...}) invece che stringhe ISO, e il backend risponde 400
// (es. in fase di creazione attività con startDate/endDate e fasce orarie).
class LocalDateAdapter : TypeAdapter<LocalDate>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalDate? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val str = reader.nextString()
        return if (str.isNullOrBlank()) null else LocalDate.parse(str, formatter)
    }
}

class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalTime? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val str = reader.nextString()
        return if (str.isNullOrBlank()) null else LocalTime.parse(str, formatter)
    }
}

object AppContainer {
    private val BASE_URL = BuildConfig.BACKEND_URL

    lateinit var sessionManager: SessionManager
        private set
    lateinit var networkMonitor: NetworkMonitor
        private set
    lateinit var appContext: Context
        private set

    val isInitialized: Boolean
        get() = ::sessionManager.isInitialized

    fun initialize(context: Context) {
        appContext = context.applicationContext
        sessionManager = SessionManager(appContext)
        networkMonitor = NetworkMonitor(appContext)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private val client by lazy {
        val cacheSize = 10 * 1024 * 1024L // 10 MB
        val cache = okhttp3.Cache(appContext.cacheDir, cacheSize)

        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(AuthInterceptor { sessionManager })
            .authenticator(TokenAuthenticator { sessionManager })
            .addInterceptor { chain ->
                var request = chain.request()
                if (!isNetworkAvailable(appContext)) {
                    request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=86400")
                        .build()
                } else {
                    request = request.newBuilder()
                        .header("Cache-Control", "public, max-age=5")
                        .build()
                }
                chain.proceed(request)
            }
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                val cacheControl = response.header("Cache-Control")
                if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")
                ) {
                    response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=60")
                        .build()
                } else {
                    response
                }
            }
            .addInterceptor(logging)
            .build()
    }

    private val gson = com.google.gson.GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val reviewApiService: ReviewApiService by lazy {
        retrofit.create(ReviewApiService::class.java)
    }

    val reviewRepository: ReviewRepository by lazy {
        ReviewRepository(reviewApiService)
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

    val userRepository: UserRepository by lazy { UserRepositoryImpl(apiService, { sessionManager }) }
    val activityRepository: ActivityRepository by lazy { ActivityRepositoryImpl(apiService) }
    val itineraryRepository: ItineraryRepository by lazy { ItineraryRepositoryImpl(apiService) }
    val localitaRepository: LocalitaRepository by lazy { LocalitaRepositoryImpl(apiService) }
}
