package com.velora.portal.journey.lending.catalog.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_apply
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.PageProductDetail
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.journey.lending.catalog.data.ApplicationRepository
import com.velora.portal.domain.credit.model.MemberOverviewResponse
import com.velora.portal.domain.credit.model.CatalogEntry
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.PageLoadState
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApplicationProcessViewModel(
    private val loanRepository: ApplicationRepository = ApplicationRepository(),
) : BaseViewModel() {

    val togetherLoanResult = MutableLiveData<List<CatalogEntry>?>()
    fun togetherLoan(
        files: List<MultipartBody.Part>,
        multipartBody: Map<String, RequestBody>,
    ) {
        createNetworkRequest {
            loanRepository.submitBundleOrder(files, multipartBody)
        }.onSuccess {
            togetherLoanResult.value = it
            submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_apply,
                    result = it.toJsonString()
                )
            )
        }.onFailed {
            loanFailResult.value = true
            submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_apply,
                    result = it.toJsonString()
                )
            )
            true
        }
    }

    val loanFailResult = MutableLiveData<Boolean>()
    val loanResult = MutableLiveData<CatalogEntry?>()
    fun loan(
        files: List<MultipartBody.Part>,
        multipartBody: Map<String, RequestBody>,
    ) {
        createNetworkRequest {
            loanRepository.submitLoanOrder(files, multipartBody)
        }.onSuccess {
            submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_apply,
                    result = it.toJsonString()
                )
            )
            loanResult.value = it
        }.onFailed {
            submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_apply,
                    result = it.toJsonString()
                )
            )
            loanFailResult.value = true
            true
        }
    }

    val togetherInfo = MutableLiveData<MemberOverviewResponse?>()
    private val _togetherLoanState = MutableLiveData<PageLoadState<MemberOverviewResponse>>(
        PageLoadState.Loading,
    )
    val togetherLoanState: LiveData<PageLoadState<MemberOverviewResponse>> = _togetherLoanState

    fun getTogetherLoan(
        showLoading: Boolean = false,
        errorAction: () -> Unit = {},
    ) {
        _togetherLoanState.value = PageLoadState.Loading
        createNetworkRequest {
            loanRepository.loadBundleLoanPage()
        }.showLoading(showLoading).onSuccess { loan ->
            submitTrackingEvent(
                TrackBean(
                    p = PageHome,
                    act = ACT_apply,
                    result = loan.toJsonString()
                )
            )
            togetherInfo.value = loan
            _togetherLoanState.value = if (loan == null) {
                PageLoadState.Error
            } else {
                PageLoadState.Content(loan)
            }
        }.onFailed {
            submitTrackingEvent(
                TrackBean(
                    p = PageHome,
                    act = ACT_apply,
                    result = it.toJsonString()
                )
            )
            errorAction.invoke()
            _togetherLoanState.value = PageLoadState.Error
            true
        }
    }
}
