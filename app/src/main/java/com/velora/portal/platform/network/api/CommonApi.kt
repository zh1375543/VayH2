package com.velora.portal.platform.network.api

import com.velora.portal.domain.credit.model.CheckoutActionResponse
import com.velora.portal.domain.customer.model.VerificationOptionResponse
import com.velora.portal.journey.communication.inbox.model.InboxPageResponse
import com.velora.portal.journey.lending.dashboard.model.PromotionBannerResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.ServiceResponse
import com.velora.portal.platform.common.data.bean.SignatureSecretResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CommonApi {
    @GET("api/user/app/common/secret")
    suspend fun requestSessionSalt(): ServiceResponse<SignatureSecretResponse?>

    @GET("api/user/app/application/config/auth/config")
    suspend fun loadAuthConfig(): ServiceResponse<VerificationOptionResponse?>

    @GET("api/user/app/activity/list")
    suspend fun loadPromotionBanners(): ServiceResponse<MutableList<PromotionBannerResponse>?>

    @GET("api/user/app/common/reloan/button/sign")
    suspend fun loadReloanGate(): ServiceResponse<CheckoutActionResponse?>

    @POST("api/data/app/fcm/sendRecord/list")
    suspend fun loadInboxMessages(@Body paramBean: ApiRequest): ServiceResponse<InboxPageResponse?>

    @POST("api/data/app/fcm/sendRecord/update")
    suspend fun markMessageRead(@Body param: ApiRequest): ServiceResponse<Any?>
}
