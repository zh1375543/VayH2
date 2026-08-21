package com.velora.portal.feature.catalog.data

import com.velora.portal.core.common.data.bean.ApiRequest
import com.velora.portal.core.network.Api
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow
import com.velora.portal.feature.catalog.model.CatalogItemBean

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
