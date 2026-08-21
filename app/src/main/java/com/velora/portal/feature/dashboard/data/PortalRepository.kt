package com.velora.portal.feature.dashboard.data

import com.velora.portal.core.common.data.bean.ApiRequest
import com.velora.portal.core.network.Api
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow
import com.velora.portal.feature.dashboard.model.PromotionBannerResponse
import com.velora.portal.feature.dashboard.model.VisitorPortalResponse
import com.velora.portal.feature.catalog.model.MemberOverviewResponse

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
