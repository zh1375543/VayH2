package com.velora.portal.journey.servicing.checkout.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.domain.credit.model.CatalogEntry
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.credit.model.CheckoutActionResponse

class RepaymentRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadBatchRepaymentOrders(): List<CatalogEntry>? {
        return api.loadBundleRepayments(ApiRequest()).dataOrThrow()
    }

    suspend fun settleBatchRepayment(orderList: List<String>): CheckoutActionResponse? {
        return api.settleBundleRepayment(ApiRequest(orderNoList = orderList)).dataOrThrow()
    }
}
