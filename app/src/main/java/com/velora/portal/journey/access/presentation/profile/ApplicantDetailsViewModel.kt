package com.velora.portal.journey.access.presentation.profile

import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_next
import com.velora.portal.platform.common.data.PageInfoPersonal
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.SelectionOption
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.journey.access.data.ApplicantVerificationRepository
import com.velora.portal.domain.customer.model.ApplicantFormOptionsResponse
import com.velora.portal.domain.customer.model.ApplicantProfileResponse
import com.velora.portal.domain.customer.model.EmploymentOptionsResponse
import com.velora.portal.platform.common.util.text.toJsonString

class ApplicantDetailsViewModel(
    private val verificationRepository: ApplicantVerificationRepository =
        ApplicantVerificationRepository(),
) : BaseViewModel() {

    val submitResult = MutableLiveData<Any?>()
    fun saveApplicantProfile(paramBean: ApiRequest) {
        createNetworkRequest { verificationRepository.saveApplicantProfile(paramBean) }
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
        createNetworkRequest { verificationRepository.loadApplicantOptions() }
            .onSuccess {
                enumBean.value = it
                it?.let(action)
            }
            .execute()
    }

    fun getAddressList(id: String? = null, action: (List<SelectionOption>) -> Unit) {
        createNetworkRequest { verificationRepository.loadRegionalDirectories(id) }
            .showLoading()
            .onSuccess { action(it ?: emptyList()) }
            .execute()
    }

    fun getWorkInfoOptions(action: (EmploymentOptionsResponse) -> Unit) {
        createNetworkRequest { verificationRepository.loadEmploymentOptions() }
            .onSuccess { it?.let(action) }
            .execute()
    }

    val personalResult = MutableLiveData<ApplicantProfileResponse?>()
    fun getPersonalInfo(errorAction: () -> Unit) {
        createNetworkRequest { verificationRepository.loadApplicantProfile() }
            .onSuccess { personalResult.value = it }
            .onFailed {
                errorAction()
                false
            }
    }
}
