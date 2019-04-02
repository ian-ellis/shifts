package com.github.ianellis.shifts.networking

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServiceFactory(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {

    fun <T> createService(clazz: Class<T>, endpoint: String): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(httpClient)
            .addConverterFactory(ShiftResponseConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        return retrofit.create(clazz)
    }
}