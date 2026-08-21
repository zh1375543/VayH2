package com.velora.portal.journey.servicing.records.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.journey.servicing.records.data.OrderRepository
import com.velora.portal.domain.credit.model.RecordDetailResponse
import com.velora.portal.domain.credit.model.LoanRecordItem
import com.velora.portal.domain.credit.model.CheckoutActionResponse
import com.velora.portal.platform.common.util.PageLoadState

class RecordCenterViewModel(
    private val loanOrderRepository: OrderRepository = OrderRepository(),
) : BaseViewModel() {

    private val _borrowingHistoryUiState = MutableLiveData<PageLoadState<List<LoanRecordItem>>>(
        PageLoadState.Loading,
    )
    val borrowingHistoryUiState: LiveData<PageLoadState<List<LoanRecordItem>>> =
        _borrowingHistoryUiState

    fun getOrderList() {
        _borrowingHistoryUiState.value = PageLoadState.Loading
        createNetworkRequest {
            loanOrderRepository.loadOrderHistory()
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
            loanOrderRepository.loadOrderDetail(orderId)
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
            loanOrderRepository.loadReloanGate()
        }.onSuccess {
            buttonResult.value = it
        }.onFailed {
            buttonResult.value = null
            false
        }
    }

    val installmentRepayResult = MutableLiveData<CheckoutActionResponse?>()
    fun loadRepaymentUrl(orderNo: String?, planNumberList: List<Int?>?) {
        createNetworkRequest {
            loanOrderRepository.loadRepaymentUrl(orderNo, planNumberList)
        }.showLoading().onSuccess {
            installmentRepayResult.value = it
        }.execute()
    }

    fun applyForReloan(id: Long?, applyAgainSign: Int?, block: () -> Unit) {
        createNetworkRequest {
            loanOrderRepository.applyForReloan(id, applyAgainSign)
        }.showLoading().onSuccess {
            block()
        }.execute()
    }

    fun cancelReloanApplication(id: Long?, block: () -> Unit) {
        createNetworkRequest {
            loanOrderRepository.cancelReloanApplication(id)
        }.showLoading().onSuccess {
            block()
        }.execute()
    }
}
