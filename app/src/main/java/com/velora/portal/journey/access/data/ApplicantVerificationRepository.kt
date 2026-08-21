package com.velora.portal.journey.access.data

import android.net.Uri
import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.data.APPCODE
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.SelectionOption
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.customer.model.IdentityDocumentResponse
import com.velora.portal.domain.customer.model.IdentityPolicyResponse
import com.velora.portal.domain.customer.model.FaceVerificationSessionResponse
import com.velora.portal.domain.customer.model.ApplicantFormOptionsResponse
import com.velora.portal.domain.customer.model.ApplicantProfileResponse
import com.velora.portal.domain.customer.model.EmploymentContactResponse
import com.velora.portal.domain.customer.model.EmploymentOptionsResponse
import com.velora.portal.platform.common.util.generateRequestBody
import com.velora.portal.platform.common.util.uriToPart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

class ApplicantVerificationRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadIdentityDocuments(): IdentityDocumentResponse? {
        return api.loadIdentityDocuments(ApiRequest()).dataOrThrow()
    }

    suspend fun createLivenessSession(): FaceVerificationSessionResponse? {
        return api.createLivenessSession(ApiRequest()).dataOrThrow()
    }

    suspend fun pollLivenessSession(bizNo: String?): FaceVerificationSessionResponse? {
        return api.pollLivenessSession(ApiRequest(bizNo = bizNo)).dataOrThrow()
    }

    suspend fun uploadIdentityDocuments(imageUri: Uri, imageType: String, cardType: String): Any? {
        val formMedia = HashMap<String, String>()
        formMedia["mobileType"] = "2"
        formMedia["appCode"] = APPCODE
        formMedia["version"] = BuildConfig.VERSION_NAME
        formMedia["imgType"] = imageType
        formMedia["cardType"] = cardType
        return api.uploadKycDocuments(
            imageUri.uriToPart("image"),
            formMedia.generateRequestBody()
        ).dataOrThrow()
    }

    suspend fun loadIdentityPolicy(): IdentityPolicyResponse? {
        return api.loadKycPolicy(ApiRequest()).dataOrThrow()
    }

    suspend fun performFaceMatch(): Any? {
        return api.performFaceMatch(ApiRequest()).dataOrThrow()
    }

    suspend fun uploadLivenessProof(faceUri: Uri, liveFile: File?): Any? {
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
        return api.uploadLivenessProof(
            livePart,
            faceUri.uriToPart("faceFile"),
            formMedia.generateRequestBody()
        ).dataOrThrow()
    }

    suspend fun saveApplicantProfile(param: ApiRequest): Any? {
        return api.saveApplicantProfile(param).dataOrThrow()
    }

    suspend fun loadApplicantOptions(): ApplicantFormOptionsResponse? {
        return api.loadApplicantOptions(ApiRequest()).dataOrThrow()
    }

    suspend fun loadRegionalDirectories(parentId: String?): List<SelectionOption> {
        return api.loadRegionalDirectories(ApiRequest(parentId = parentId))
            .dataOrThrow()
            ?.map { SelectionOption(it.name.orEmpty(), id = it.id) }
            ?: emptyList()
    }

    suspend fun loadEmploymentOptions(): EmploymentOptionsResponse? {
        return api.loadEmploymentOptions(ApiRequest()).dataOrThrow()
    }

    suspend fun loadEmploymentContact(): EmploymentContactResponse? {
        return api.loadEmploymentContact(ApiRequest()).dataOrThrow()
    }

    suspend fun savePayoutAndContact(param: ApiRequest): Any? {
        return api.savePayoutAndContact(param).dataOrThrow()
    }

    suspend fun startCarrierOtpStep(phone: String, company: String): Any? {
        return api.startTelecomOtpStep(ApiRequest(phone = phone, company = company)).dataOrThrow()
    }

    suspend fun finishCarrierOtpStep(phone: String, company: String, otp: String): Any? {
        return api.finishTelecomOtpStep(
            ApiRequest(phone = phone, company = company, otp = otp)
        ).dataOrThrow()
    }

    suspend fun advanceCarrierOtpStep(
        phone: String,
        company: String,
        otp: String,
    ): Any? {
        return api.continueTelecomOtpStep(
            ApiRequest(phone = phone, company = company, otp = otp)
        ).dataOrThrow()
    }

    suspend fun saveSupplementaryInfo(param: ApiRequest): Any? {
        return api.saveSupplementaryInfo(param).dataOrThrow()
    }

    suspend fun loadApplicantProfile(): ApplicantProfileResponse? {
        return api.loadApplicantProfile(ApiRequest()).dataOrThrow()
    }
}
