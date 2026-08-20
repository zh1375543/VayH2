package com.novexa.platform.feature.dashboard.presentation

import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.core.common.data.ACT_common
import com.novexa.platform.core.common.data.PageHome
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.feature.dashboard.data.DefaultPortalRepository
import com.novexa.platform.feature.dashboard.data.PortalRepository
import com.novexa.platform.feature.dashboard.model.VisitorPortalResponse
import com.novexa.platform.core.common.util.text.toJsonString

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
