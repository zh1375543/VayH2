package com.velora.portal.platform.network

import com.velora.portal.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Provides the shared HTTP client and Retrofit services used by the application. */
object NetworkProvider {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(NetworkInterceptor())
            .addInterceptor(LogInterceptor())
            // Parameter completion must run before signing so the final payload is signed.
            .addInterceptor(ParamsInterceptor())
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(SignInterceptor())
            .addInterceptor(TrackInterceptor())
            .connectTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .build()
    }

    val api: Api by lazy {
        createService(Api::class.java, BuildConfig.HTTP_HOST)
    }

    val calcuPageApi: CalcuPageApi by lazy {
        createService(CalcuPageApi::class.java, BuildConfig.HTTP_HOST)
    }

    val trackApi: ApiTrack by lazy {
        createService(ApiTrack::class.java, BuildConfig.TRACK_HOST)
    }

    private fun <T> createService(
        serviceClass: Class<T>,
        baseUrl: String,
    ): T {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
            .create(serviceClass)
    }
}
