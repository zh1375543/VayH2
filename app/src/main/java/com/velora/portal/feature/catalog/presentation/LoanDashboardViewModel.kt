package com.velora.portal.feature.catalog.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_approvalDenied
import com.velora.portal.platform.common.data.ACT_approvalInProgress
import com.velora.portal.platform.common.data.ACT_index
import com.velora.portal.platform.common.data.HomeLoanAmountRange
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.PageHomePre
import com.velora.portal.platform.common.data.PageHomeRefuse
import com.velora.portal.platform.common.data.bean.Event
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.feature.dashboard.data.DefaultPortalRepository
import com.velora.portal.feature.dashboard.data.PortalRepository
import com.velora.portal.feature.dashboard.presentation.state.CreditStage
import com.velora.portal.feature.dashboard.presentation.state.HomeEffect
import com.velora.portal.feature.dashboard.presentation.state.HomeEntryTracker
import com.velora.portal.feature.dashboard.presentation.state.MemberHomeUiState
import com.velora.portal.feature.dashboard.presentation.state.toMemberHomeUiState
import com.velora.portal.feature.catalog.model.MemberOverviewResponse
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.PageLoadState
import kotlinx.coroutines.Job

/** Supplies the loan dashboard payload to loan- and repayment-related screens. */
class LoanDashboardViewModel(
    private val homeRepository: PortalRepository = DefaultPortalRepository(),
) : BaseViewModel() {

    val authResult = MutableLiveData<MemberOverviewResponse?>()
    private val _authDataState = MutableLiveData<PageLoadState<MemberOverviewResponse>>(
        PageLoadState.Loading,
    )
    val authDataState: LiveData<PageLoadState<MemberOverviewResponse>> = _authDataState
    val memberHomeState = MutableLiveData<MemberHomeUiState>()
    val homeEffect = MutableLiveData<Event<HomeEffect>>()
    val loadFailedResult = MutableLiveData<Unit>()

    private var authJob: Job? = null

    fun getAuthData(isLoading: Boolean = false) {
        loadAuthData(isLoading = isLoading, renderMemberHome = false)
    }

    /** Loads the dashboard specifically for HomeFragment and publishes home-owned UI models. */
    fun getMemberHomeData(isLoading: Boolean = false) {
        loadAuthData(isLoading = isLoading, renderMemberHome = true)
    }

    private fun loadAuthData(isLoading: Boolean, renderMemberHome: Boolean) {
        authJob?.cancel()
        _authDataState.value = PageLoadState.Loading
        authJob = createNetworkRequest {
            homeRepository.loadMemberHome()
        }.showLoading(isLoading).onSuccess {
            HomeLoanAmountRange = it?.loanAmountRange
            submitTrackingEvent(
                TrackBean(
                    p = PageHome,
                    act = ACT_index,
                    result = it.toJsonString(),
                ),
            )
            authResult.value = it
            _authDataState.value = if (it == null) {
                PageLoadState.Error
            } else {
                PageLoadState.Content(it)
            }
            if (renderMemberHome) {
                it?.let { response ->
                    val state = response.toMemberHomeUiState()
                    memberHomeState.value = state
                    trackCreditStage(state)
                    publishHomeEffects(state)
                }
            }
        }.onFailed {
            loadFailedResult.value = Unit
            _authDataState.value = PageLoadState.Error
            submitTrackingEvent(
                TrackBean(
                    p = PageHome,
                    act = ACT_index,
                    result = it.toJsonString(),
                ),
            )
            true
        }
    }

    private fun trackCreditStage(state: MemberHomeUiState) {
        val tracking = when (state.creditStage) {
            CreditStage.REVIEWING -> TrackBean(
                p = PageHomePre,
                act = ACT_approvalInProgress,
                result = System.currentTimeMillis().toString(),
            )

            CreditStage.REJECTED -> TrackBean(
                p = PageHomeRefuse,
                act = ACT_approvalDenied,
                result = System.currentTimeMillis().toString(),
            )

            CreditStage.APPROVED -> null
        }
        tracking?.let(::submitTrackingEvent)
    }

    private fun publishHomeEffects(state: MemberHomeUiState) {
        if (state.hasPendingRepayment) {
            emitHomeEffect(HomeEffect.ShowAppRating)
        }

        val shouldNavigateToOrders =
            state.hasRepaymentProducts && HomeEntryTracker.consumeFirstEntry()
        if (shouldNavigateToOrders) {
            emitHomeEffect(HomeEffect.NavigateToOrders)
            return
        }

        when {
            state.newProducts.isNotEmpty() -> {
                emitHomeEffect(HomeEffect.ShowNewProducts(state.newProducts))
            }

            state.canShowAvailableCreditDialog -> {
                emitHomeEffect(
                    HomeEffect.ShowAvailableCredit(
                        amount = state.availableAmount,
                        currencySymbol = state.creditCurrencySymbol ?: state.fallbackCurrencySymbol,
                    ),
                )
            }
        }
    }

    private fun emitHomeEffect(effect: HomeEffect) {
        homeEffect.value = Event(effect)
    }
}
