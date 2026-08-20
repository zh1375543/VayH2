package com.novexa.platform.core.network

import com.novexa.platform.core.common.data.bean.ServiceResponse
import com.novexa.platform.feature.onboarding.model.RegionalDirectoryResponse
import com.novexa.platform.feature.onboarding.model.VerificationOptionResponse
import com.novexa.platform.feature.accounts.model.LinkedAccountResponse
import com.novexa.platform.feature.dashboard.model.PromotionBannerResponse
import com.novexa.platform.feature.onboarding.model.EmploymentContactResponse
import com.novexa.platform.feature.dashboard.model.VisitorPortalResponse
import com.novexa.platform.feature.catalog.model.MemberOverviewResponse
import com.novexa.platform.feature.onboarding.model.IdentityPolicyResponse
import com.novexa.platform.feature.onboarding.model.FaceVerificationSessionResponse
import com.novexa.platform.feature.onboarding.model.IdentityDocumentResponse
import com.novexa.platform.feature.onboarding.model.AccessSessionResponse
import com.novexa.platform.feature.inbox.model.InboxPageResponse
import com.novexa.platform.feature.records.model.RecordDetailResponse
import com.novexa.platform.feature.records.model.RecordItemBean
import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.feature.accounts.model.AccountChannelResponse
import com.novexa.platform.feature.onboarding.model.ApplicantProfileResponse
import com.novexa.platform.feature.onboarding.model.ApplicantFormOptionsResponse
import com.novexa.platform.feature.catalog.model.CatalogItemBean
import com.novexa.platform.core.common.data.bean.SignatureSecretResponse
import com.novexa.platform.feature.checkout.model.CheckoutActionResponse
import com.novexa.platform.feature.onboarding.model.VerificationProgressResponse
import com.novexa.platform.feature.accounts.model.AccountMethodResponse
import com.novexa.platform.feature.onboarding.model.EmploymentOptionsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface Api {

    @GET("api/user/app/common/secret")
    suspend fun fetchSecret(): ServiceResponse<SignatureSecretResponse?>

    @POST("api/user/app/login/sms")
    suspend fun sendSMS(@Body param: ApiRequest): ServiceResponse<Any?>

    @POST("api/loan/app/common/index")
    suspend fun fetchHomeData(@Body param: ApiRequest = ApiRequest()): ServiceResponse<VisitorPortalResponse?>

    @POST("api/user/app/login")
    suspend fun login(@Body param: ApiRequest): ServiceResponse<AccessSessionResponse?>

    @POST("api/user/app/delete/user")
    suspend fun logout(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/password/set")
    suspend fun fetchPassword(@Body param: ApiRequest): ServiceResponse<AccessSessionResponse?>

    @POST("api/user/app/password/update")
    suspend fun updatePassword(@Body param: ApiRequest): ServiceResponse<AccessSessionResponse?>

    @POST("api/user/app/userEquipment/save")
    suspend fun postDeviceInfo(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/userAuth/detail")
    suspend fun fetchUserAuth(@Body paramBean: ApiRequest = ApiRequest()): ServiceResponse<VerificationProgressResponse?>

    @POST("api/user/app/kyc/info")
    suspend fun fetchKycInfo(@Body paramBean: ApiRequest): ServiceResponse<IdentityDocumentResponse?>

    @POST("api/user/app/userBaseExt/getEnum")
    suspend fun fetchPersonalInfoEnum(@Body paramBean: ApiRequest): ServiceResponse<ApplicantFormOptionsResponse?>

    @POST("api/user/app/userBaseExt/info")
    suspend fun fetchPersonalInfo(@Body paramBean: ApiRequest): ServiceResponse<ApplicantProfileResponse?>

    @POST("api/user/app/address/list")
    suspend fun fetchAddressList(@Body paramBean: ApiRequest): ServiceResponse<MutableList<RegionalDirectoryResponse>?>

    @POST("api/user/app/userBaseExt/save/v2")
    suspend fun postPersonalInfo(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/userWork/enum")
    suspend fun fetchWorkInfoEnum(@Body paramBean: ApiRequest): ServiceResponse<EmploymentOptionsResponse?>

    @POST("api/user/app/userWork/info")
    suspend fun fetchContactInfo(@Body param: ApiRequest): ServiceResponse<EmploymentContactResponse?>

    @POST("api/user/app/userWork/save")
    suspend fun postContactInfo(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/list")
    suspend fun fetchPayChannel(@Body paramBean: ApiRequest): ServiceResponse<MutableList<AccountChannelResponse>?>

    @POST("api/user/app/wallet/list")
    suspend fun getWalletList(@Body param: ApiRequest): ServiceResponse<MutableList<AccountMethodResponse>?>

    @POST("api/user/app/userCashWallet/list/my")
    suspend fun getMyWalletList(@Body param: ApiRequest): ServiceResponse<MutableList<AccountMethodResponse>?>

    @POST("api/user/app/userCashWallet/set/default")
    suspend fun setDefaultWallet(@Body param: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/bind")
    suspend fun bindCard(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/myCard")
    suspend fun fetchBankcardList(@Body paramBean: ApiRequest): ServiceResponse<MutableList<LinkedAccountResponse>?>

    @POST("api/user/app/bank/addBank")
    suspend fun addCard(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/unbind")
    suspend fun unbindCard(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/setDefault")
    suspend fun fetchCardDefault(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/loan/app/index/v3")
    suspend fun fetchHomeLoan(@Body paramBean: ApiRequest = ApiRequest()): ServiceResponse<MemberOverviewResponse?>

    @Multipart
    @POST("api/loan/app/order/commit/all/with/event")
    suspend fun oneLoanApply(
        @Part files: List<MultipartBody.Part>,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<MutableList<CatalogItemBean>?>

    @Multipart
    @POST("api/loan/app/order/commit/with/event")
    suspend fun loanApply(
        @Part files: List<MultipartBody.Part>,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<CatalogItemBean?>

    @POST("api/loan/app/productInfo/detail")
    suspend fun fetchProductDetail(@Body paramBean: ApiRequest): ServiceResponse<CatalogItemBean?>

    @POST("api/loan/app/order/oldList")
    suspend fun fetchOrderList(@Body paramBean: ApiRequest = ApiRequest()): ServiceResponse<MutableList<RecordItemBean>?>

    @POST("api/loan/app/order/detail")
    suspend fun fetchOrderDetail(@Body paramBean: ApiRequest): ServiceResponse<RecordDetailResponse?>

    @POST("api/data/app/fcm/sendRecord/list")
    suspend fun fetchMessageList(@Body paramBean: ApiRequest): ServiceResponse<InboxPageResponse?>

    @POST("api/data/app/fcm/sendRecord/update")
    suspend fun updateMessageStatus(@Body param: ApiRequest): ServiceResponse<Any?>

    @GET("api/user/app/application/config/auth/config")
    suspend fun fetchAuthentication(): ServiceResponse<VerificationOptionResponse?>

    @POST("api/loan/app/index/loan/page")
    suspend fun togetherLoan(@Body param: ApiRequest = ApiRequest()): ServiceResponse<MemberOverviewResponse?>

    @POST("api/user/app/userDevice/save")
    suspend fun saveUserDevice(@Body riskRequestBody: RequestBody): ServiceResponse<Any?>

    @POST("api/user/app/userDevice/hasDevice")
    suspend fun hasUserDevice(@Body paramBean: ApiRequest): ServiceResponse<Boolean?>

    @POST("api/user/app/vietnam/telecom/otp/one")
    suspend fun fetchTeleOtpOne(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/vietnam/telecom/otp/two")
    suspend fun fetchTeleOtpTwo(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/vietnam/telecom/otp/three")
    suspend fun submitTeleOtp(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/bind/v2")
    suspend fun submitBankAndContactInfo(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @GET("api/user/app/activity/list")
    suspend fun fetchBannerList(): ServiceResponse<MutableList<PromotionBannerResponse>?>

    @POST("api/user/app/userBaseExt/save/work/v2")
    suspend fun submitSuppleInfo(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/finance/app/multiple/order/list")
    suspend fun togetherRepaymentList(@Body paramBean: ApiRequest): ServiceResponse<MutableList<CatalogItemBean>?>

    @POST("api/finance/app/multiple/order/repay")
    suspend fun togetherRepayment(@Body paramBean: ApiRequest): ServiceResponse<CheckoutActionResponse?>

    @GET("api/user/app/common/reloan/button/sign")
    suspend fun showRepaymentBorrow(): ServiceResponse<CheckoutActionResponse?>

    @POST("api/finance/app/order/repay/url")
    suspend fun installmentRepay(@Body paramBean: ApiRequest): ServiceResponse<CheckoutActionResponse?>

    @POST("api/loan/app/apply/again")
    suspend fun repayAndBorrow(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/loan/app/apply/again/cancel")
    suspend fun cancelApply(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/kyc/config")
    suspend fun fetchKycConfig(@Body paramBean: ApiRequest): ServiceResponse<IdentityPolicyResponse?>

    @POST("api/user/app/kyc/face/compare")
    suspend fun faceCompare(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @Multipart
    @POST("api/user/app/kyc/save/v2")
    suspend fun submitKycImage(
        @Part image: MultipartBody.Part,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<Any?>

    @Multipart
    @POST("api/user/app/kyc/liveness/anti/hack")
    suspend fun submitLiveness(
        @Part livenessDataFile: MultipartBody.Part?,
        @Part faceFile: MultipartBody.Part,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<Any?>

    @POST("api/user/app/score/review")
    suspend fun submitFeedback(@Body feedback: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/user/liveness/result/get")
    suspend fun getH5Result(@Body p: ApiRequest): ServiceResponse<FaceVerificationSessionResponse?>

    @POST("api/user/app/user/liveness/h5/get")
    suspend fun fetchH5Live(@Body p: ApiRequest): ServiceResponse<FaceVerificationSessionResponse?>
}
