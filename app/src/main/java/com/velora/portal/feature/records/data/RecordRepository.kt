package com.velora.portal.feature.records.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.feature.records.model.RecordDetailResponse
import com.velora.portal.feature.records.model.RecordItemBean
import com.velora.portal.feature.checkout.model.CheckoutActionResponse

class RecordRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchOrderList(): List<RecordItemBean> {
        return api.fetchOrderList().dataOrThrow() ?: emptyList()
    }

    suspend fun fetchOrderDetail(orderId: Long?): RecordDetailResponse? {
        return api.fetchOrderDetail(ApiRequest(orderId = orderId)).dataOrThrow()
    }

    suspend fun fetchRepaymentBorrowButtonState(): String? {
        return api.showRepaymentBorrow().dataOrThrow()?.reloanButtonSign
    }

    suspend fun installmentRepay(
        orderNo: String?,
        planNumberList: List<Int?>?,
    ): CheckoutActionResponse? {
        return api.installmentRepay(
            ApiRequest(
                orderNo = orderNo,
                planNumList = planNumberList,
            )
        ).dataOrThrow()
    }

    suspend fun repayAndBorrow(orderId: Long?, applyAgainSign: Int?): Any? {
        return api.repayAndBorrow(
            ApiRequest(orderId = orderId, applyAgainSign = applyAgainSign)
        ).dataOrThrow()
    }

    suspend fun cancelApply(orderId: Long?): Any? {
        return api.cancelApply(ApiRequest(orderId = orderId)).dataOrThrow()
    }
}
