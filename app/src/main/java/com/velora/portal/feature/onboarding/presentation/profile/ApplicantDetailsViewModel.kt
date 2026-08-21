package com.velora.portal.feature.onboarding.presentation.profile

import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_next
import com.velora.portal.platform.common.data.PageInfoPersonal
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.SelectionOption
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.feature.onboarding.data.DocumentReviewRepository
import com.velora.portal.feature.onboarding.model.ApplicantFormOptionsResponse
import com.velora.portal.feature.onboarding.model.ApplicantProfileResponse
import com.velora.portal.feature.onboarding.model.EmploymentOptionsResponse
import com.velora.portal.platform.common.util.text.toJsonString

class ApplicantDetailsViewModel(
    private val verificationRepository: DocumentReviewRepository =
        DocumentReviewRepository(),
) : BaseViewModel() {

    val submitResult = MutableLiveData<Any?>()
    fun submitPersonalInfo(paramBean: ApiRequest) {
        createNetworkRequest { verificationRepository.submitPersonalInfo(paramBean) }
            .showLoading()
            .onSuccess {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_next,
                        result = it.toJsonString()
                    )
                )
                submitResult.value = it
            }
            .onFailed {
                submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_next,
                        result = it.toJsonString()
                    )
                )
                false
            }
    }

    private val enumBean = MutableLiveData<ApplicantFormOptionsResponse?>()
    fun getEnums(action: (ApplicantFormOptionsResponse) -> Unit) {
        enumBean.value?.let {
            action(it)
            return
        }
        createNetworkRequest { verificationRepository.fetchPersonalInfoOptions() }
            .onSuccess {
                enumBean.value = it
                it?.let(action)
            }
            .execute()
    }

    fun getAddressList(id: String? = null, action: (List<SelectionOption>) -> Unit) {
        createNetworkRequest { verificationRepository.fetchAddressOptions(id) }
            .showLoading()
            .onSuccess { action(it ?: emptyList()) }
            .execute()
    }

    fun getWorkInfoOptions(action: (EmploymentOptionsResponse) -> Unit) {
        createNetworkRequest { verificationRepository.fetchWorkInfoOptions() }
            .onSuccess { it?.let(action) }
            .execute()
    }

    val personalResult = MutableLiveData<ApplicantProfileResponse?>()
    fun getPersonalInfo(errorAction: () -> Unit) {
        createNetworkRequest { verificationRepository.fetchPersonalInfo() }
            .onSuccess { personalResult.value = it }
            .onFailed {
                errorAction()
                false
            }
    }
}
