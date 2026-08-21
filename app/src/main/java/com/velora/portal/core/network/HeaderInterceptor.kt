package com.velora.portal.core.network

import com.velora.portal.core.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request().newBuilder()
            .header("Content-Type", "application/json")
            .header("Content-Encoding", "gzip")
            .header("User-Agent", "Android")
            .header("lang", "en_US")
        if (SessionStore.token.isNotBlank()) {
            originalRequest.addHeader("Authorization", SessionStore.token)
        }
        val appCheckToken = NetworkCredentialStore.appCheckToken
        if (appCheckToken.isNotBlank()) {
            originalRequest.header("X-Firebase-AppCheck", appCheckToken)
        }
        return chain.proceed(originalRequest.build())
    }
}
