package com.novexa.platform.feature.records.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.records.model.RecordDetailResponse
import com.novexa.platform.feature.records.model.RecordItemBean
import com.novexa.platform.feature.checkout.model.CheckoutActionResponse

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
