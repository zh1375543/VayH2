package com.velora.portal.platform.network.api

import com.velora.portal.domain.credit.model.CatalogEntry
import com.velora.portal.domain.credit.model.MemberOverviewResponse
import com.velora.portal.domain.credit.model.RecordDetailResponse
import com.velora.portal.domain.credit.model.LoanRecordItem
import com.velora.portal.journey.lending.dashboard.model.VisitorPortalResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.ServiceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface LoanApi {
    @POST("api/loan/app/common/index")
    suspend fun loadPortalOverview(@Body param: ApiRequest = ApiRequest()): ServiceResponse<VisitorPortalResponse?>

    @POST("api/loan/app/index/v3")
    suspend fun loadMemberOverview(@Body paramBean: ApiRequest = ApiRequest()): ServiceResponse<MemberOverviewResponse?>

    @Multipart
    @POST("api/loan/app/order/commit/all/with/event")
    suspend fun submitBundleOrder(
        @Part files: List<MultipartBody.Part>,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<MutableList<CatalogEntry>?>

    @Multipart
    @POST("api/loan/app/order/commit/with/event")
    suspend fun submitLoanOrder(
        @Part files: List<MultipartBody.Part>,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<CatalogEntry?>

    @POST("api/loan/app/productInfo/detail")
    suspend fun loadProductDetail(@Body paramBean: ApiRequest): ServiceResponse<CatalogEntry?>

    @POST("api/loan/app/order/oldList")
    suspend fun loadOrderHistory(@Body paramBean: ApiRequest = ApiRequest()): ServiceResponse<MutableList<LoanRecordItem>?>

    @POST("api/loan/app/order/detail")
    suspend fun loadOrderDetail(@Body paramBean: ApiRequest): ServiceResponse<RecordDetailResponse?>

    @POST("api/loan/app/index/loan/page")
    suspend fun loadBundleLoanPage(@Body param: ApiRequest = ApiRequest()): ServiceResponse<MemberOverviewResponse?>

    @POST("api/loan/app/apply/again")
    suspend fun applyForReloan(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/loan/app/apply/again/cancel")
    suspend fun cancelReloanApplication(@Body paramBean: ApiRequest): ServiceResponse<Any?>
}
