package com.velora.portal.platform.network

import com.velora.portal.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TrackInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return if (isTrackRequest(request)) {
            chain.proceed(attachTrackHeaders(request))
        } else {
            chain.proceed(request)
        }
    }

    private fun isTrackRequest(request: Request): Boolean {
        return request.url.toString().contains(BuildConfig.TRACK_HOST)
    }

    private fun attachTrackHeaders(request: Request): Request {
        val rawBodySize = request.body?.contentLength()?.toString() ?: "0"
        return request.newBuilder()
            .addHeader("x-log-apiversion", "0.6.0")
            .addHeader("x-log-bodyrawsize", rawBodySize)
            .addHeader("Connection", "keep-alive")
            .build()
    }
}
