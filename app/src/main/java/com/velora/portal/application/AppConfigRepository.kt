package com.velora.portal.application

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.SignatureSecretResponse
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AppConfigRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchAppSecret(): SignatureSecretResponse? {
        return api.requestSessionSalt().dataOrThrow()
    }

    suspend fun hasUploadedDevice(): Boolean? {
        return api.hasDeviceFingerprint(ApiRequest()).dataOrThrow()
    }

    suspend fun uploadRiskInfo(riskJson: String): Any? {
        // The exact media type is part of the signed request body.
        val body = riskJson.toRequestBody(
            "application/json; charset=utf-8".toMediaTypeOrNull()
        )
        return api.recordDeviceFingerprint(body).dataOrThrow()
    }
}
