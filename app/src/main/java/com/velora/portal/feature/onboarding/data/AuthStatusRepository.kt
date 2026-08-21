package com.velora.portal.feature.onboarding.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.feature.onboarding.model.VerificationProgressResponse

class AuthStatusRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadUserAuthStatus(): VerificationProgressResponse? {
        return api.fetchUserAuth(ApiRequest()).dataOrThrow()
    }

    suspend fun loadAuthConfigList(): List<String> {
        return api.fetchAuthentication()
            .dataOrThrow()
            ?.authConfig
            ?.split(",")
            ?.map { it.trim().uppercase() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }
}
