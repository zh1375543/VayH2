package com.novexa.platform.feature.dashboard.presentation

import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.core.common.data.ACT_common
import com.novexa.platform.core.common.data.PageHome
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.feature.dashboard.data.DefaultPortalRepository
import com.novexa.platform.feature.dashboard.data.PortalRepository
import com.novexa.platform.feature.dashboard.model.PromotionBannerResponse
import com.novexa.platform.feature.dashboard.model.VisitorPortalResponse
import com.novexa.platform.core.common.util.text.toJsonString

/** Owns data requests and transient screen state that are exclusive to HomeFragment. */
class HomeViewModel(
    private val homeRepository: PortalRepository = DefaultPortalRepository(),
) : BaseViewModel() {

    val guestResult = MutableLiveData<VisitorPortalResponse?>()
    val loadFailedResult = MutableLiveData<Unit>()
    val bannerResult = MutableLiveData<List<PromotionBannerResponse>>()

    fun getUnAuthData(showLoading: Boolean = false) {
        createNetworkRequest {
            homeRepository.loadGuestHome()
        }.showLoading(showLoading).onSuccess {
            guestResult.value = it
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

    fun getBannerList() {
        createNetworkRequest {
            homeRepository.loadBanners()
        }.onSuccess {
            bannerResult.value = it ?: emptyList()
        }.onFailed { true }
    }
}
