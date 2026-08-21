package com.velora.portal.platform.network

import com.velora.portal.platform.common.data.APPCODE
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.toMd5
import org.json.JSONArray
import org.json.JSONObject
import java.util.SortedMap
import java.util.TreeMap

private val EMOJI_CODE_UNITS = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+")
private const val SIGNATURE_PART_SEPARATOR = "*|*"

/** Creates the signature headers expected by the API. */
class SignatureGenerator(
    private val timestampProvider: () -> String = { System.currentTimeMillis().toString() },
    private val signingSecretProvider: () -> String = { NetworkCredentialStore.signingSecret },
) {

    fun generate(payload: String): SignatureHeaders {
        val requestTimestamp = timestampProvider()
        val canonicalPayload = sortJson(JSONObject(resolvePayload(payload))).toJsonString()
        val input = buildRawSignature(canonicalPayload, requestTimestamp)

        return SignatureHeaders(
            sign = digest(stripEmoji(input)),
            timestamp = requestTimestamp,
        )
    }

    private fun resolvePayload(payload: String): String {
        return if (payload.isBlank() || payload == "{}") ApiRequest().toJsonString() else payload
    }

    private fun buildRawSignature(payload: String, timestamp: String): String {
        return listOf(APPCODE.toMd5(), signingSecretProvider(), payload, timestamp)
            .joinToString(SIGNATURE_PART_SEPARATOR)
    }

    private fun stripEmoji(value: String): String = value.replace(EMOJI_CODE_UNITS, "")

    private fun digest(value: String): String = value.toMd5()

    /** Deep-sorts object keys while retaining the original array order. */
    private fun sortJson(source: JSONObject): SortedMap<String, Any?> {
        val orderedValues = TreeMap<String, Any?>()
        val keys = source.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            orderedValues[key] = when (val item = source.get(key)) {
                is JSONObject -> sortJson(item)
                is JSONArray -> (0 until item.length()).map { position ->
                    item.get(position).let { element ->
                        if (element is JSONObject) sortJson(element) else element
                    }
                }
                else -> item
            }
        }
        return orderedValues
    }
}

data class SignatureHeaders(
    val sign: String,
    val timestamp: String,
)
