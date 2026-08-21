package com.velora.portal.platform.network

import com.velora.portal.platform.common.util.isNetworkAvailable
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NetworkInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        requireNetworkConnection()
        return chain.proceed(chain.request())
    }

    private fun requireNetworkConnection() {
        if (!isNetworkAvailable()) throw NoNetworkException("No internet connection")
    }
}

class NoNetworkException(message: String) : IOException(message)
