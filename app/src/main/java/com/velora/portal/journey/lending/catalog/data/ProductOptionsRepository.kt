package com.velora.portal.journey.lending.catalog.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.credit.model.CatalogEntry

class ProductOptionsRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadProductDetail(
        productId: String?,
        amount: String?,
    ): CatalogEntry? {
        return api.loadProductDetail(
            ApiRequest(
                productId = productId,
                amount = amount,
            )
        ).dataOrThrow()
    }
}
