package com.github.ianellis.shifts

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServiceFactory constructor(
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {

    fun <T> createService(clazz: Class<T>, endpoint: String): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(httpClient)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(clazz)
    }
}