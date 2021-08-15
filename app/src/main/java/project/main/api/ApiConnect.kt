package project.main.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiConnect {


    private var apiService: ApiService? = null

    fun getService(context: Context): ApiService {
        if (apiService == null) {
            apiService = init(context)
        }
        return apiService ?: init(context)
    }

    fun resetService(context: Context): ApiService {
        apiService = init(context)
        return apiService ?: init(context)
    }

    private fun init(context: Context): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
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