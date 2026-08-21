package com.velora.portal.platform.network

import com.velora.portal.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 30L
private const val READ_TIMEOUT_SECONDS = 60L
private const val WRITE_TIMEOUT_SECONDS = 60L

/** Owns the Retrofit services and their shared HTTP configuration. */
object NetworkProvider {

    private val sharedClient: OkHttpClient by lazy(::buildOkHttpClient)

    val api: Api by lazy {
        createService(Api::class.java, BuildConfig.HTTP_HOST)
    }

    val calcuPageApi: CalcuPageApi by lazy {
        createService(CalcuPageApi::class.java, BuildConfig.HTTP_HOST)
    }

    val trackApi: ApiTrack by lazy {
        createService(ApiTrack::class.java, BuildConfig.TRACK_HOST)
    }

    private fun buildOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            installInterceptors(this)
            connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }.build()
    }

    private fun installInterceptors(clientBuilder: OkHttpClient.Builder) {
        clientBuilder.addInterceptor(NetworkInterceptor())
        clientBuilder.addInterceptor(LogInterceptor())
        clientBuilder.addInterceptor(ParamsInterceptor())
        clientBuilder.addInterceptor(HeaderInterceptor())
        clientBuilder.addInterceptor(SignInterceptor())
        clientBuilder.addInterceptor(TrackInterceptor())
    }

    private fun <T> createService(serviceType: Class<T>, endpoint: String): T {
        return createRetrofit(endpoint).create(serviceType)
    }

    private fun createRetrofit(endpoint: String): Retrofit {
        return Retrofit.Builder().apply {
            client(sharedClient)
            installJsonConverter()
            baseUrl(endpoint)
        }.build()
    }

    private fun Retrofit.Builder.installJsonConverter() {
        addConverterFactory(GsonConverterFactory.create())
    }
}
