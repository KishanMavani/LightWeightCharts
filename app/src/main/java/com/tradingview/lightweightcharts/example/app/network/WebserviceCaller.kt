package com.tradingview.lightweightcharts.example.app.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WebserviceCaller {
    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val okHttpClient: OkHttpClient by lazy {

        OkHttpClient.Builder().apply {
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            addInterceptor(loggingInterceptor)
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl("https://backend.lynxbeta.com")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    @JvmStatic
    fun getClient(): ApiService {
        return apiService
    }

}