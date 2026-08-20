package com.novexa.platform.feature.onboarding.presentation.kyc

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.core.common.data.ACT_next
import com.novexa.platform.core.common.data.ACT_uploadBack
import com.novexa.platform.core.common.data.ACT_uploadFace
import com.novexa.platform.core.common.data.ACT_uploadFront
import com.novexa.platform.core.common.data.PageInfoKyc
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.feature.onboarding.data.DocumentReviewRepository
import com.novexa.platform.feature.onboarding.model.IdentityDocumentResponse
import com.novexa.platform.feature.onboarding.model.IdentityPolicyResponse
import com.novexa.platform.feature.onboarding.model.FaceVerificationSessionResponse
import com.novexa.platform.core.ui.image.UiImageSource
import com.novexa.platform.core.common.util.text.toJsonString
import java.io.File

class KycUploadViewModel(
    private val verificationRepository: DocumentReviewRepository =
        DocumentReviewRepository(),
) : BaseViewModel() {

    val kycResult = MutableLiveData<IdentityDocumentResponse?>()
    val frontImageSource = MutableLiveData<UiImageSource?>()
    val backImageSource = MutableLiveData<UiImageSource?>()
    val selfImageSource = MutableLiveData<UiImageSource?>()
    val frontUploadSuccess = MutableLiveData(false)
    val backUploadSuccess = MutableLiveData(false)
    val selfUploadSuccess = MutableLiveData(false)
    val frontUploadFailed = MutableLiveData(false)
    val backUploadFailed = MutableLiveData(false)
    val selfUploadFailed = MutableLiveData(false)

    fun getKycInfo(errorAction: () -> Unit) {
        createNetworkRequest { verificationRepository.fetchKycDocument() }
            .onSuccess {
                kycResult.value = it
                frontImageSource.value = it?.frontImageUrl.toRemoteImageSource()
                backImageSource.value = it?.backImageUrl.toRemoteImageSource()
                selfImageSource.value = it?.liveImageUrl.toRemoteImageSource()
            }
            .onFailed {
                errorAction()
                false
            }
    }

    val h5Live = MutableLiveData<FaceVerificationSessionResponse>()
    fun fetchH5Live(error: () -> Unit) {
        createNetworkRequest { verificationRepository.createLivenessWebSession() }
            .showLoading()
            .onSuccess { h5Live.value = it }
            .onFailed {
                error()
                false
            }
    }

    val h5Result = MutableLiveData<String?>()
    fun getH5LiveResult() {
        createNetworkRequest { verificationRepository.fetchLivenessResult(h5Live.value?.bizNo) }
            .showLoading()
            .onSuccess {
                h5Result.value = it?.faceUrl
                selfImageSource.value = it?.faceUrl.toRemoteImageSource()
                selfUploadSuccess.value = !it?.faceUrl.isNullOrBlank()
            }
            .execute()
    }

    val submitFrontResult = MutableLiveData<Uri>()
    fun submitKycFront(frontUri: Uri, cardType: String) {
        frontUploadFailed.value = false
        createNetworkRequest {
            verificationRepository.uploadKycImage(frontUri, "IDCARD_CARD_FRONT", cardType)
        }
            .showLoading()
            .onSuccess {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadFront,
                        result = it.toJsonString()
                    )
                )
                submitFrontResult.value = frontUri
                frontImageSource.value = UiImageSource.LocalUri(frontUri)
                frontUploadSuccess.value = true
                frontUploadFailed.value = false
            }
            .onFailed {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadFront,
                        result = it.toJsonString()
                    )
                )
                frontUploadSuccess.value = false
                frontUploadFailed.value = true
                false
            }
    }

    val submitBackResult = MutableLiveData<Uri>()
    fun submitKycBack(backUri: Uri, cardType: String) {
        backUploadFailed.value = false
        createNetworkRequest {
            verificationRepository.uploadKycImage(backUri, "IDCARD_CARD_BACK", cardType)
        }
            .showLoading()
            .onSuccess {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadBack,
                        result = it.toJsonString()
                    )
                )
                submitBackResult.value = backUri
                backImageSource.value = UiImageSource.LocalUri(backUri)
                backUploadSuccess.value = true
                backUploadFailed.value = false
            }
            .onFailed {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadBack,
                        result = it.toJsonString()
                    )
                )
                backUploadSuccess.value = false
                backUploadFailed.value = true
                false
            }
    }

    val configResult = MutableLiveData<IdentityPolicyResponse?>()
    fun getKycConfig() {
        createNetworkRequest { verificationRepository.fetchKycConfig() }
            .onSuccess { configResult.value = it }
            .execute()
    }

    val compareResult = MutableLiveData<Any?>()
    fun compareFace() {
        createNetworkRequest { verificationRepository.compareFace() }
            .showLoading()
            .onSuccess {
                submitTrackingEvent(TrackBean(p = PageInfoKyc, act = ACT_next, result = it.toJsonString()))
                compareResult.value = it
            }
            .onFailed {
                submitTrackingEvent(TrackBean(p = PageInfoKyc, act = ACT_next, result = it.toJsonString()))
                false
            }
    }

    val submitSelfResult = MutableLiveData<Uri?>()
    fun submitKycSelf(uri: Uri, liveFile: File?) {
        selfUploadFailed.value = false
        createNetworkRequest { verificationRepository.uploadLiveness(uri, liveFile) }
            .showLoading()
            .onSuccess {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadFace,
                        result = it.toJsonString()
                    )
                )
                submitSelfResult.value = uri
                selfImageSource.value = UiImageSource.LocalUri(uri)
                selfUploadSuccess.value = true
                selfUploadFailed.value = false
            }
            .onFailed {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadFace,
                        result = it.toJsonString()
                    )
                )
                selfUploadSuccess.value = false
                selfUploadFailed.value = true
                false
            }
    }

    private fun String?.toRemoteImageSource(): UiImageSource? =
        takeUnless(String?::isNullOrBlank)?.let(UiImageSource::RemoteUrl)
}
