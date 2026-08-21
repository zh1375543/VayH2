package com.velora.portal.platform.network

import com.velora.portal.platform.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .header("Content-Type", "application/json")
            .header("Content-Encoding", "gzip")
            .header("User-Agent", "Android")
            .header("lang", "en_US")

        if (SessionStore.token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", SessionStore.token)
        }

        val appCheckToken = NetworkCredentialStore.appCheckToken
        if (appCheckToken.isNotBlank()) {
            requestBuilder.header("X-Firebase-AppCheck", appCheckToken)
        }

        return chain.proceed(requestBuilder.build())
    }
}
