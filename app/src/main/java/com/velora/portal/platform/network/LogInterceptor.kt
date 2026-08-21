package com.velora.portal.platform.network

import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.util.LogUtil
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class LogInterceptor : Interceptor {
    private val httpLogger = createHttpLogger()

    override fun intercept(chain: Interceptor.Chain): Response {
        val incomingRequest = chain.request()
        return if (incomingRequest.url.toString().contains("/track")) {
            chain.proceed(incomingRequest)
        } else {
            httpLogger.intercept(chain)
        }
    }

    private fun createHttpLogger(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            LogUtil.e("HttpIt -> $message")
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}
