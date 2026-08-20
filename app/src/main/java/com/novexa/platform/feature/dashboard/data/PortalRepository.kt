package com.novexa.platform.feature.dashboard.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.dashboard.model.PromotionBannerResponse
import com.novexa.platform.feature.dashboard.model.VisitorPortalResponse
import com.novexa.platform.feature.catalog.model.MemberOverviewResponse

interface PortalRepository {
    suspend fun loadGuestHome(): VisitorPortalResponse

    suspend fun loadMemberHome(): MemberOverviewResponse

    suspend fun loadBanners(): List<PromotionBannerResponse>
}

class DefaultPortalRepository(
    private val api: Api = NetworkProvider.api,
) : PortalRepository {

    override suspend fun loadGuestHome(): VisitorPortalResponse {
        return requireNotNull(api.fetchHomeData(ApiRequest()).dataOrThrow())
    }

    override suspend fun loadMemberHome(): MemberOverviewResponse {
        return requireNotNull(api.fetchHomeLoan(ApiRequest()).dataOrThrow())
    }

    override suspend fun loadBanners(): List<PromotionBannerResponse> {
        return api.fetchBannerList().dataOrThrow().orEmpty()
    }
}
