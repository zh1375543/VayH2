package com.velora.portal.platform.network

import com.velora.portal.platform.common.util.LogUtil
import com.velora.portal.platform.common.util.text.toJsonString
import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer

/** Extracts the final request payload used by the signing protocol. */
class RequestPayloadExtractor {

    fun extract(request: Request): String {
        val requestBody = request.body
        return when {
            requestBody?.contentType()?.subtype == "json" -> extractJsonPayload(requestBody)
            requestBody is FormBody -> extractFormPayload(request, requestBody)
            requestBody is MultipartBody -> extractMultipartPayload(request, requestBody)
            else -> ""
        }
    }

    private fun extractJsonPayload(body: RequestBody): String = readBody(body)

    private fun extractFormPayload(request: Request, formBody: FormBody): String {
        val fields = mutableMapOf<String, Any?>()
        for (position in 0 until formBody.size) {
            fields[formBody.name(position)] = formBody.value(position)
        }
        appendQueryParameters(request, fields)
        return fields.toJsonString()
    }

    private fun extractMultipartPayload(request: Request, multipartBody: MultipartBody): String {
        val fields = mutableMapOf<String, Any?>()
        multipartBody.parts.forEach { part ->
            val contentDisposition = part.headers?.get("Content-Disposition") ?: return@forEach
            if (!contentDisposition.contains("form-data; name=")) return@forEach

            val fieldName = contentDisposition.substringAfter("name=\"").substringBefore("\"")
            if (contentDisposition.contains("filename=\"")) return@forEach

            val fieldValue = readBody(part.body)
            if (fieldValue.isNotBlank() && fieldName != "eventFile") {
                fields[fieldName] = fieldValue
            }
        }
        appendQueryParameters(request, fields)
        return fields.toJsonString()
    }

    private fun appendQueryParameters(request: Request, target: MutableMap<String, Any?>) {
        request.url.queryParameterNames.forEach { parameterName ->
            target[parameterName] = request.url.queryParameter(parameterName)
        }
    }

    private fun readBody(body: RequestBody): String {
        if (body.isOneShot() || body.isDuplex()) {
            LogUtil.e("Skip signing payload extraction for a one-shot or duplex request body")
            return ""
        }
        return try {
            Buffer().use { buffer ->
                body.writeTo(buffer)
                buffer.readUtf8()
            }
        } catch (error: Exception) {
            LogUtil.e("Read request body for signing failed: ${error.message}")
            ""
        }
    }
}
