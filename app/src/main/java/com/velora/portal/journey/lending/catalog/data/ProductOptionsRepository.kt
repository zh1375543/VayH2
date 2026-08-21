package com.velora.portal.journey.lending.catalog.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.credit.model.CatalogItemBean

class ProductOptionsRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchProductDetail(
        productId: String?,
        amount: String?,
    ): CatalogItemBean? {
        return api.fetchProductDetail(
            ApiRequest(
                productId = productId,
                amount = amount,
            )
        ).dataOrThrow()
    }
}
