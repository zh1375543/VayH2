package com.velora.portal.app

import com.velora.portal.core.common.data.bean.ApiRequest
import com.velora.portal.core.common.data.bean.SignatureSecretResponse
import com.velora.portal.core.network.Api
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AppConfigRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchAppSecret(): SignatureSecretResponse? {
        return api.fetchSecret().dataOrThrow()
    }

    suspend fun hasUploadedDevice(): Boolean? {
        return api.hasUserDevice(ApiRequest()).dataOrThrow()
    }

    suspend fun uploadRiskInfo(riskJson: String): Any? {
        // The exact media type is part of the signed request body.
        val body = riskJson.toRequestBody(
            "application/json; charset=utf-8".toMediaTypeOrNull()
        )
        return api.saveUserDevice(body).dataOrThrow()
    }
}
