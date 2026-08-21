package com.velora.portal.platform.network

import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.util.LogUtil
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class LogInterceptor : Interceptor {
    private val loggingInterceptor = HttpLoggingInterceptor {
        LogUtil.e("HttpIt -> $it")
    }.apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return if (request.url.toString().contains("/track")) {
            chain.proceed(request)
        } else {
            loggingInterceptor.intercept(chain)
        }
    }
}
