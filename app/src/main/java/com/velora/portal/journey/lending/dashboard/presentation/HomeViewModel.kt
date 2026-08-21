package com.velora.portal.journey.lending.dashboard.presentation

import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_common
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.journey.lending.dashboard.data.DefaultPortalRepository
import com.velora.portal.journey.lending.dashboard.data.PortalRepository
import com.velora.portal.journey.lending.dashboard.model.PromotionBannerResponse
import com.velora.portal.journey.lending.dashboard.model.VisitorPortalResponse
import com.velora.portal.platform.common.util.text.toJsonString

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
