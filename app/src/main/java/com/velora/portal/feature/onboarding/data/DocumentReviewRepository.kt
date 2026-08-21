package com.velora.portal.feature.onboarding.data

import android.net.Uri
import com.velora.portal.BuildConfig
import com.velora.portal.core.common.data.APPCODE
import com.velora.portal.core.common.data.bean.ApiRequest
import com.velora.portal.core.common.data.bean.SelectionOption
import com.velora.portal.core.network.Api
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow
import com.velora.portal.feature.onboarding.model.IdentityDocumentResponse
import com.velora.portal.feature.onboarding.model.IdentityPolicyResponse
import com.velora.portal.feature.onboarding.model.FaceVerificationSessionResponse
import com.velora.portal.feature.onboarding.model.ApplicantFormOptionsResponse
import com.velora.portal.feature.onboarding.model.ApplicantProfileResponse
import com.velora.portal.feature.onboarding.model.EmploymentContactResponse
import com.velora.portal.feature.onboarding.model.EmploymentOptionsResponse
import com.velora.portal.core.common.util.generateRequestBody
import com.velora.portal.core.common.util.uriToPart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

class DocumentReviewRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchKycDocument(): IdentityDocumentResponse? {
        return api.fetchKycInfo(ApiRequest()).dataOrThrow()
    }

    suspend fun createLivenessWebSession(): FaceVerificationSessionResponse? {
        return api.fetchH5Live(ApiRequest()).dataOrThrow()
    }

    suspend fun fetchLivenessResult(bizNo: String?): FaceVerificationSessionResponse? {
        return api.getH5Result(ApiRequest(bizNo = bizNo)).dataOrThrow()
    }

    suspend fun uploadKycImage(imageUri: Uri, imageType: String, cardType: String): Any? {
        val formMedia = HashMap<String, String>()
        formMedia["mobileType"] = "2"
        formMedia["appCode"] = APPCODE
        formMedia["version"] = BuildConfig.VERSION_NAME
        formMedia["imgType"] = imageType
        formMedia["cardType"] = cardType
        return api.submitKycImage(
            imageUri.uriToPart("image"),
            formMedia.generateRequestBody()
        ).dataOrThrow()
    }

    suspend fun fetchKycConfig(): IdentityPolicyResponse? {
        return api.fetchKycConfig(ApiRequest()).dataOrThrow()
    }

    suspend fun compareFace(): Any? {
        return api.faceCompare(ApiRequest()).dataOrThrow()
    }

    suspend fun uploadLiveness(faceUri: Uri, liveFile: File?): Any? {
        val formMedia = HashMap<String, String>()
        formMedia["mobileType"] = "2"
        formMedia["appCode"] = APPCODE
        formMedia["version"] = BuildConfig.VERSION_NAME
        formMedia["imageId"] = UUID.randomUUID().toString()
        var livePart: MultipartBody.Part? = null
        if (liveFile != null) {
            val requestBody = liveFile.asRequestBody("image/*".toMediaTypeOrNull())
            livePart = MultipartBody.Part.createFormData(
                "livenessDataFile",
                liveFile.name,
                requestBody
            )
        }
        return api.submitLiveness(
            livePart,
            faceUri.uriToPart("faceFile"),
            formMedia.generateRequestBody()
        ).dataOrThrow()
    }

    suspend fun submitPersonalInfo(param: ApiRequest): Any? {
        return api.postPersonalInfo(param).dataOrThrow()
    }

    suspend fun fetchPersonalInfoOptions(): ApplicantFormOptionsResponse? {
        return api.fetchPersonalInfoEnum(ApiRequest()).dataOrThrow()
    }

    suspend fun fetchAddressOptions(parentId: String?): List<SelectionOption> {
        return api.fetchAddressList(ApiRequest(parentId = parentId))
            .dataOrThrow()
            ?.map { SelectionOption(it.name.orEmpty(), id = it.id) }
            ?: emptyList()
    }

    suspend fun fetchWorkInfoOptions(): EmploymentOptionsResponse? {
        return api.fetchWorkInfoEnum(ApiRequest()).dataOrThrow()
    }

    suspend fun fetchContactInfo(): EmploymentContactResponse? {
        return api.fetchContactInfo(ApiRequest()).dataOrThrow()
    }

    suspend fun submitBankAndContactInfo(param: ApiRequest): Any? {
        return api.submitBankAndContactInfo(param).dataOrThrow()
    }

    suspend fun requestCarrierOtp(phone: String, company: String): Any? {
        return api.fetchTeleOtpOne(ApiRequest(phone = phone, company = company)).dataOrThrow()
    }

    suspend fun submitCarrierOtp(phone: String, company: String, otp: String): Any? {
        return api.submitTeleOtp(
            ApiRequest(phone = phone, company = company, otp = otp)
        ).dataOrThrow()
    }

    suspend fun submitCarrierOtpAndRequestNext(
        phone: String,
        company: String,
        otp: String,
    ): Any? {
        return api.fetchTeleOtpTwo(
            ApiRequest(phone = phone, company = company, otp = otp)
        ).dataOrThrow()
    }

    suspend fun submitSupplementInfo(param: ApiRequest): Any? {
        return api.submitSuppleInfo(param).dataOrThrow()
    }

    suspend fun fetchPersonalInfo(): ApplicantProfileResponse? {
        return api.fetchPersonalInfo(ApiRequest()).dataOrThrow()
    }
}
