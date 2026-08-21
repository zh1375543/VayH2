package com.velora.portal.journey.access.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.customer.model.VerificationProgressResponse

class AuthStatusRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadUserAuthStatus(): VerificationProgressResponse? {
        return api.loadVerificationProgress(ApiRequest()).dataOrThrow()
    }

    suspend fun loadAuthConfigList(): List<String> {
        return api.loadAuthConfig()
            .dataOrThrow()
            ?.authConfig
            ?.split(",")
            ?.map { it.trim().uppercase() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }
}
