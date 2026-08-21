package com.velora.portal.platform.network

import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.data.APPCODE
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/** Completes protocol parameters for the request methods that currently use them. */
class ParamsInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val incomingRequest = chain.request()
        return applyParameters(chain, incomingRequest, buildCommonParameters())
    }

    private fun buildCommonParameters(): Map<String, String> {
        return mapOf(
            "appCode" to APPCODE,
            "version" to BuildConfig.VERSION_NAME,
            "mobileType" to "2",
        )
    }

    private fun applyParameters(
        chain: Interceptor.Chain,
        request: Request,
        protocolParameters: Map<String, String>,
    ): Response {
        return if (request.method.uppercase() == "GET") {
            appendMissingQueryParameters(chain, request, protocolParameters)
        } else {
            chain.proceed(request)
        }
    }

    private fun appendMissingQueryParameters(
        chain: Interceptor.Chain,
        request: Request,
        protocolParameters: Map<String, String>,
    ): Response {
        val urlBuilder = request.url.newBuilder()
        val existingNames = request.url.queryParameterNames

        protocolParameters.forEach { (name, value) ->
            if (!existingNames.contains(name)) {
                urlBuilder.addQueryParameter(name, value)
            }
        }
        return chain.proceed(request.newBuilder().url(urlBuilder.build()).build())
    }
}
