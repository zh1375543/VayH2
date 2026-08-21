package com.velora.portal.journey.lending.dashboard.presentation

import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_common
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.journey.lending.dashboard.data.DefaultPortalRepository
import com.velora.portal.journey.lending.dashboard.data.PortalRepository
import com.velora.portal.journey.lending.dashboard.model.VisitorPortalResponse
import com.velora.portal.platform.common.util.text.toJsonString

/** Loads guest configuration reused by login and customer-support entry points. */
class VisitorPortalViewModel(
    private val homeRepository: PortalRepository = DefaultPortalRepository(),
) : BaseViewModel() {

    val result = MutableLiveData<VisitorPortalResponse?>()
    val loadFailedResult = MutableLiveData<Unit>()

    fun getUnAuthData(showLoading: Boolean = false) {
        createNetworkRequest {
            homeRepository.loadGuestHome()
        }.showLoading(showLoading).onSuccess {
            result.value = it
            submitTrackingEvent(
                TrackBean(
                    p = PageHome,
                    act = ACT_common,
                    result = it.toJsonString(),
                ),
            )
        }.onFailed {
            loadFailedResult.value = Unit
            submitTrackingEvent(
                TrackBean(
                    p = PageHome,
                    act = ACT_common,
                    result = it.toJsonString(),
                ),
            )
            true
        }
    }
}
