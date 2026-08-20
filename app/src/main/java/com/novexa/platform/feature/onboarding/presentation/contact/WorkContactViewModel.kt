package com.novexa.platform.feature.onboarding.presentation.contact

import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.core.common.data.ACT_next
import com.novexa.platform.core.common.data.PageInfoBank
import com.novexa.platform.core.common.data.PageSupplementaryInformation
import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.feature.onboarding.data.DocumentReviewRepository
import com.novexa.platform.feature.onboarding.model.EmploymentContactResponse
import com.novexa.platform.feature.onboarding.model.EmploymentOptionsResponse
import com.novexa.platform.core.common.util.text.toJsonString

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
