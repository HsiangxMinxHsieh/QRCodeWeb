package project.main.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiConnect {

    private var apiService: ApiService? = null

    fun getService(): ApiService {
        if (apiService == null) {
            apiService = init()
        }
        return apiService ?: init()
    }

    fun resetService(context: Context): ApiService {
        apiService = init()
        return apiService ?: init()
    }

    private fun init(): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

//
        val retrofit = Retrofit.Builder()
            .baseUrl("https://google.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}