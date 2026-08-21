package com.velora.portal.journey.servicing.records.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.credit.model.RecordDetailResponse
import com.velora.portal.domain.credit.model.LoanRecordItem
import com.velora.portal.domain.credit.model.CheckoutActionResponse

class OrderRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadOrderHistory(): List<LoanRecordItem> {
        return api.loadOrderHistory().dataOrThrow() ?: emptyList()
    }

    suspend fun loadOrderDetail(orderId: Long?): RecordDetailResponse? {
        return api.loadOrderDetail(ApiRequest(orderId = orderId)).dataOrThrow()
    }

    suspend fun loadReloanGate(): String? {
        return api.loadReloanGate().dataOrThrow()?.reloanButtonSign
    }

    suspend fun loadRepaymentUrl(
        orderNo: String?,
        planNumberList: List<Int?>?,
    ): CheckoutActionResponse? {
        return api.fetchRepaymentUrl(
            ApiRequest(
                orderNo = orderNo,
                planNumList = planNumberList,
            )
        ).dataOrThrow()
    }

    suspend fun applyForReloan(orderId: Long?, applyAgainSign: Int?): Any? {
        return api.applyForReloan(
            ApiRequest(orderId = orderId, applyAgainSign = applyAgainSign)
        ).dataOrThrow()
    }

    suspend fun cancelReloanApplication(orderId: Long?): Any? {
        return api.cancelReloanApplication(ApiRequest(orderId = orderId)).dataOrThrow()
    }
}
