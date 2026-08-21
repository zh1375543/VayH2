package com.velora.portal.feature.checkout.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.feature.catalog.model.CatalogItemBean
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
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
