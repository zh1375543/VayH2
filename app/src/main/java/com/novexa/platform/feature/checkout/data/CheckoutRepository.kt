package com.novexa.platform.feature.checkout.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.feature.catalog.model.CatalogItemBean
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.checkout.model.CheckoutActionResponse

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
