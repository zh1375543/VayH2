package com.novexa.platform.feature.onboarding.presentation.profile

import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.core.common.data.ACT_next
import com.novexa.platform.core.common.data.PageInfoPersonal
import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.common.data.bean.SelectionOption
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.feature.onboarding.data.DocumentReviewRepository
import com.novexa.platform.feature.onboarding.model.ApplicantFormOptionsResponse
import com.novexa.platform.feature.onboarding.model.ApplicantProfileResponse
import com.novexa.platform.feature.onboarding.model.EmploymentOptionsResponse
import com.novexa.platform.core.common.util.text.toJsonString

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
