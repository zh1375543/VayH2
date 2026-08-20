package com.novexa.platform.feature.onboarding.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.onboarding.model.VerificationProgressResponse

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
