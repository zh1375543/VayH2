package com.novexa.platform.feature.records.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.feature.records.data.RecordRepository
import com.novexa.platform.feature.records.model.RecordDetailResponse
import com.novexa.platform.feature.records.model.RecordItemBean
import com.novexa.platform.feature.checkout.model.CheckoutActionResponse
import com.novexa.platform.core.common.util.PageLoadState

class RecordCenterViewModel(
    private val loanOrderRepository: RecordRepository = RecordRepository(),
) : BaseViewModel() {

    private val _borrowingHistoryUiState = MutableLiveData<PageLoadState<List<RecordItemBean>>>(
        PageLoadState.Loading,
    )
    val borrowingHistoryUiState: LiveData<PageLoadState<List<RecordItemBean>>> =
        _borrowingHistoryUiState

    fun getOrderList() {
        _borrowingHistoryUiState.value = PageLoadState.Loading
        createNetworkRequest {
            loanOrderRepository.fetchOrderList()
        }.onSuccess { result ->
            val orders = result.orEmpty()
            _borrowingHistoryUiState.value = if (orders.isEmpty()) {
                PageLoadState.Empty
            } else {
                PageLoadState.Content(orders)
            }
        }.onFailed {
            _borrowingHistoryUiState.value = PageLoadState.Error
            true
        }
    }

    private val _orderDetailState = MutableLiveData<PageLoadState<RecordDetailResponse>>(
        PageLoadState.Loading,
    )
    val orderDetailState: LiveData<PageLoadState<RecordDetailResponse>> = _orderDetailState

    fun getOrderDetail(orderId: Long?) {
        _orderDetailState.value = PageLoadState.Loading
        createNetworkRequest {
            loanOrderRepository.fetchOrderDetail(orderId)
        }.onSuccess { detail ->
            _orderDetailState.value = if (detail == null) {
                PageLoadState.Error
            } else {
                PageLoadState.Content(detail)
            }
        }.onFailed {
            _orderDetailState.value = PageLoadState.Error
            true
        }
    }

    val buttonResult = MutableLiveData<String?>()
    fun getButtonState() {
        createNetworkRequest {
            loanOrderRepository.fetchRepaymentBorrowButtonState()
        }.onSuccess {
            buttonResult.value = it
        }.onFailed {
            buttonResult.value = null
            false
        }
    }

    val installmentRepayResult = MutableLiveData<CheckoutActionResponse?>()
    fun installmentRepay(orderNo: String?, planNumberList: List<Int?>?) {
        createNetworkRequest {
            loanOrderRepository.installmentRepay(orderNo, planNumberList)
        }.showLoading().onSuccess {
            installmentRepayResult.value = it
        }.execute()
    }

    fun repayAndBorrow(id: Long?, applyAgainSign: Int?, block: () -> Unit) {
        createNetworkRequest {
            loanOrderRepository.repayAndBorrow(id, applyAgainSign)
        }.showLoading().onSuccess {
            block()
        }.execute()
    }

    fun cancelApply(id: Long?, block: () -> Unit) {
        createNetworkRequest {
            loanOrderRepository.cancelApply(id)
        }.showLoading().onSuccess {
            block()
        }.execute()
    }
}
