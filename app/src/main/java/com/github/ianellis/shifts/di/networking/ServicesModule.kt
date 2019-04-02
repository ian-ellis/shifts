package com.github.ianellis.shifts.di.networking

import com.github.ianellis.shifts.domain.ShiftsService
import com.github.ianellis.shifts.networking.ServiceFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ServicesModule {


    companion object {
        private const val ENDPOINT = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/"
        private const val READ_TIMEOUT_SECONDS = 5L
        private const val CONNECTION_TIMEOUT_SECONDS = 5L
        private const val WRITE_TIMEOUT_SECONDS = 5L
        private const val NAME_SHA = "57a33a5496950fec8433e4dd83347673459dcdfc"
    }

    @Provides
    @Singleton
    fun providesGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun providesOkHttp(): OkHttpClient {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                requestBuilder.method(original.method(), original.body())
                requestBuilder.header("Authorization", NAME_SHA)
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    fun providesServiceFactory(gson: Gson, okHttp: OkHttpClient): ServiceFactory {
        return ServiceFactory(gson, okHttp)
    }

    @Provides
    fun providesShiftsService(factory: ServiceFactory): ShiftsService {
        return factory.createService(ShiftsService::class.java, ENDPOINT)
    }
}