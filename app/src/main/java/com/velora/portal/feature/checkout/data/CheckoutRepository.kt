package com.velora.portal.feature.checkout.data

import com.velora.portal.core.common.data.bean.ApiRequest
import com.velora.portal.feature.catalog.model.CatalogItemBean
import com.velora.portal.core.network.Api
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow
import com.velora.portal.feature.checkout.model.CheckoutActionResponse

class CheckoutRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchBatchRepaymentOrders(): List<CatalogItemBean>? {
        return api.togetherRepaymentList(ApiRequest()).dataOrThrow()
    }

    suspend fun submitBatchRepayment(orderList: List<String>): CheckoutActionResponse? {
        return api.togetherRepayment(ApiRequest(orderNoList = orderList)).dataOrThrow()
    }
}
