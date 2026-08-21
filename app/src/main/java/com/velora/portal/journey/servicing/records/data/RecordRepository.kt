package com.velora.portal.journey.servicing.records.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.credit.model.RecordDetailResponse
import com.velora.portal.domain.credit.model.RecordItemBean
import com.velora.portal.domain.credit.model.CheckoutActionResponse

class RecordRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchOrderList(): List<RecordItemBean> {
        return api.loadOrderHistory().dataOrThrow() ?: emptyList()
    }

    suspend fun fetchOrderDetail(orderId: Long?): RecordDetailResponse? {
        return api.loadOrderDetail(ApiRequest(orderId = orderId)).dataOrThrow()
    }

    suspend fun fetchRepaymentBorrowButtonState(): String? {
        return api.loadReloanGate().dataOrThrow()?.reloanButtonSign
    }

    suspend fun installmentRepay(
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

    suspend fun repayAndBorrow(orderId: Long?, applyAgainSign: Int?): Any? {
        return api.applyForReloan(
            ApiRequest(orderId = orderId, applyAgainSign = applyAgainSign)
        ).dataOrThrow()
    }

    suspend fun cancelApply(orderId: Long?): Any? {
        return api.cancelReloanApplication(ApiRequest(orderId = orderId)).dataOrThrow()
    }
}
