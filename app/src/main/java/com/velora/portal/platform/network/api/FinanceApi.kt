package com.velora.portal.platform.network.api

import com.velora.portal.domain.credit.model.CatalogItemBean
import com.velora.portal.domain.credit.model.CheckoutActionResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FinanceApi {
    @POST("api/finance/app/multiple/order/list")
    suspend fun loadBundleRepayments(@Body paramBean: ApiRequest): ServiceResponse<MutableList<CatalogItemBean>?>

    @POST("api/finance/app/multiple/order/repay")
    suspend fun settleBundleRepayment(@Body paramBean: ApiRequest): ServiceResponse<CheckoutActionResponse?>

    @POST("api/finance/app/order/repay/url")
    suspend fun fetchRepaymentUrl(@Body paramBean: ApiRequest): ServiceResponse<CheckoutActionResponse?>
}
