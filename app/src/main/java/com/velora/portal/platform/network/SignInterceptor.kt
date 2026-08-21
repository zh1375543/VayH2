package com.velora.portal.platform.network

import com.velora.portal.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val SIGN_HEADER = "sign"
private const val TIMESTAMP_HEADER = "timestamp"

/** Adds request-signing headers once all earlier interceptors have completed. */
class SignInterceptor(
    private val payloadExtractor: RequestPayloadExtractor = RequestPayloadExtractor(),
    private val signatureGenerator: SignatureGenerator = SignatureGenerator(),
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return if (shouldBypass(request)) {
            chain.proceed(request)
        } else {
            chain.proceed(attachSignature(request))
        }
    }

    private fun shouldBypass(request: Request): Boolean {
        return request.url.toString().contains(BuildConfig.TRACK_HOST)
    }

    private fun attachSignature(request: Request): Request {
        val signatureHeaders = signatureGenerator.generate(payloadExtractor.extract(request))
        return request.newBuilder()
            .addHeader(SIGN_HEADER, signatureHeaders.sign)
            .addHeader(TIMESTAMP_HEADER, signatureHeaders.timestamp)
            .build()
    }
}
