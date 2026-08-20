package com.novexa.platform.feature.catalog.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.catalog.model.CatalogItemBean

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
