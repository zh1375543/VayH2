package com.velora.portal.platform.network.api

import com.velora.portal.domain.customer.model.ApplicantFormOptionsResponse
import com.velora.portal.domain.customer.model.ApplicantProfileResponse
import com.velora.portal.domain.customer.model.EmploymentContactResponse
import com.velora.portal.domain.customer.model.EmploymentOptionsResponse
import com.velora.portal.domain.customer.model.FaceVerificationSessionResponse
import com.velora.portal.domain.customer.model.IdentityDocumentResponse
import com.velora.portal.domain.customer.model.IdentityPolicyResponse
import com.velora.portal.domain.customer.model.RegionalDirectoryResponse
import com.velora.portal.domain.customer.model.VerificationProgressResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.ServiceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface UserApi {
    @POST("api/user/app/userEquipment/save")
    suspend fun persistDeviceSnapshot(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/userAuth/detail")
    suspend fun loadVerificationProgress(@Body paramBean: ApiRequest = ApiRequest()): ServiceResponse<VerificationProgressResponse?>

    @POST("api/user/app/kyc/info")
    suspend fun loadIdentityDocuments(@Body paramBean: ApiRequest): ServiceResponse<IdentityDocumentResponse?>

    @POST("api/user/app/userBaseExt/getEnum")
    suspend fun loadApplicantOptions(@Body paramBean: ApiRequest): ServiceResponse<ApplicantFormOptionsResponse?>

    @POST("api/user/app/userBaseExt/info")
    suspend fun loadApplicantProfile(@Body paramBean: ApiRequest): ServiceResponse<ApplicantProfileResponse?>

    @POST("api/user/app/address/list")
    suspend fun loadRegionalDirectories(@Body paramBean: ApiRequest): ServiceResponse<MutableList<RegionalDirectoryResponse>?>

    @POST("api/user/app/userBaseExt/save/v2")
    suspend fun saveApplicantProfile(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/userWork/enum")
    suspend fun loadEmploymentOptions(@Body paramBean: ApiRequest): ServiceResponse<EmploymentOptionsResponse?>

    @POST("api/user/app/userWork/info")
    suspend fun loadEmploymentContact(@Body param: ApiRequest): ServiceResponse<EmploymentContactResponse?>

    @POST("api/user/app/userWork/save")
    suspend fun saveEmploymentContact(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/vietnam/telecom/otp/one")
    suspend fun startTelecomOtpStep(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/vietnam/telecom/otp/two")
    suspend fun continueTelecomOtpStep(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/vietnam/telecom/otp/three")
    suspend fun finishTelecomOtpStep(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/userDevice/save")
    suspend fun recordDeviceFingerprint(@Body riskRequestBody: RequestBody): ServiceResponse<Any?>

    @POST("api/user/app/userDevice/hasDevice")
    suspend fun hasDeviceFingerprint(@Body paramBean: ApiRequest): ServiceResponse<Boolean?>

    @POST("api/user/app/bank/bind/v2")
    suspend fun savePayoutAndContact(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/userBaseExt/save/work/v2")
    suspend fun saveSupplementaryInfo(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/kyc/config")
    suspend fun loadKycPolicy(@Body paramBean: ApiRequest): ServiceResponse<IdentityPolicyResponse?>

    @POST("api/user/app/kyc/face/compare")
    suspend fun performFaceMatch(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @Multipart
    @POST("api/user/app/kyc/save/v2")
    suspend fun uploadKycDocuments(
        @Part image: MultipartBody.Part,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<Any?>

    @Multipart
    @POST("api/user/app/kyc/liveness/anti/hack")
    suspend fun uploadLivenessProof(
        @Part livenessDataFile: MultipartBody.Part?,
        @Part faceFile: MultipartBody.Part,
        @PartMap multipartBody: Map<String, @JvmSuppressWildcards RequestBody>,
    ): ServiceResponse<Any?>

    @POST("api/user/app/score/review")
    suspend fun sendRatingFeedback(@Body feedback: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/user/liveness/result/get")
    suspend fun pollLivenessSession(@Body p: ApiRequest): ServiceResponse<FaceVerificationSessionResponse?>

    @POST("api/user/app/user/liveness/h5/get")
    suspend fun createLivenessSession(@Body p: ApiRequest): ServiceResponse<FaceVerificationSessionResponse?>
}
