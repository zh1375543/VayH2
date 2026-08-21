package com.velora.portal.feature.onboarding.presentation.contact

import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_next
import com.velora.portal.platform.common.data.PageInfoBank
import com.velora.portal.platform.common.data.PageSupplementaryInformation
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.feature.onboarding.data.DocumentReviewRepository
import com.velora.portal.feature.onboarding.model.EmploymentContactResponse
import com.velora.portal.feature.onboarding.model.EmploymentOptionsResponse
import com.velora.portal.platform.common.util.text.toJsonString

class WorkContactViewModel(
    private val verificationRepository: DocumentReviewRepository =
        DocumentReviewRepository(),
) : BaseViewModel() {

    fun getContactEnum(action: (EmploymentOptionsResponse) -> Unit) {
        createNetworkRequest { verificationRepository.fetchWorkInfoOptions() }
            .showLoading()
            .onSuccess { it?.let(action) }
            .execute()
    }

    val contractResult = MutableLiveData<EmploymentContactResponse?>()
    fun getContactsInfo(errorAction: () -> Unit = {}) {
        createNetworkRequest { verificationRepository.fetchContactInfo() }
            .onSuccess { contractResult.value = it }
            .onFailed {
                errorAction()
                true
            }
    }

    val submitBankAndCtsResult = MutableLiveData<Any?>()
    fun submitBankAndCtsInfo(paramBean: ApiRequest) {
        createNetworkRequest { verificationRepository.submitBankAndContactInfo(paramBean) }
            .showLoading()
            .onSuccess {
                submitTrackingEvent(TrackBean(p = PageInfoBank, act = ACT_next, result = it.toJsonString()))
                submitBankAndCtsResult.value = it
            }
            .onFailed {
                submitTrackingEvent(TrackBean(p = PageInfoBank, act = ACT_next, result = it.toJsonString()))
                false
            }
    }

    val submitSuppleInfoResult = MutableLiveData<Any?>()
    fun submitSuppleInfo(paramBean: ApiRequest) {
        createNetworkRequest { verificationRepository.submitSupplementInfo(paramBean) }
            .showLoading()
            .onSuccess {
                submitTrackingEvent(
                    TrackBean(
                        p = PageSupplementaryInformation,
                        act = ACT_next,
                        result = it.toJsonString()
                    )
                )
                submitSuppleInfoResult.value = it
            }
            .onFailed {
                submitTrackingEvent(
                    TrackBean(
                        p = PageSupplementaryInformation,
                        act = ACT_next,
                        result = it.toJsonString()
                    )
                )
                false
            }
    }
}
