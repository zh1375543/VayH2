package com.velora.portal.feature.checkout.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.velora.portal.core.ui.base.BaseViewModel
import com.velora.portal.feature.catalog.model.CatalogItemBean
import com.velora.portal.feature.checkout.data.CheckoutRepository
import com.velora.portal.feature.checkout.model.CheckoutActionResponse
import com.velora.portal.core.common.util.PageLoadState
import com.velora.portal.core.common.util.text.formatAmountWithPrefix
import java.math.BigDecimal

class CheckoutViewModel(
    private val repaymentRepository: CheckoutRepository = CheckoutRepository(),
) : BaseViewModel() {

    private val _orderListState = MutableLiveData<PageLoadState<List<CatalogItemBean>>>(
        PageLoadState.Loading,
    )
    val orderListState: LiveData<PageLoadState<List<CatalogItemBean>>> = _orderListState
    private val selectedOrderKeys = mutableSetOf<String>()
    private var hasLoadedBatchOrders = false
    private val _selectedOrderCount = MutableLiveData("0")
    val selectedOrderCount: LiveData<String> = _selectedOrderCount
    private val _selectedOrderAmount = MutableLiveData("0")
    val selectedOrderAmount: LiveData<String> = _selectedOrderAmount

    fun getOrderList() {
        _orderListState.value = PageLoadState.Loading
        createNetworkRequest {
            repaymentRepository.fetchBatchRepaymentOrders()
        }.onSuccess { result ->
            val orders = result.orEmpty().map { order ->
                order.copy(
                    isCheck = if (hasLoadedBatchOrders) {
                        order.selectionKey() in selectedOrderKeys
                    } else {
                        true
                    },
                )
            }
            hasLoadedBatchOrders = hasLoadedBatchOrders || orders.isNotEmpty()
            updateBatchSelection(orders)
            _orderListState.value = PageLoadState.Content(orders)
        }.onFailed {
            _orderListState.value = PageLoadState.Error
            true
        }
    }

    fun updateBatchSelection(orders: List<CatalogItemBean>) {
        val selectedOrders = orders.filter(CatalogItemBean::isCheck)
        selectedOrderKeys.clear()
        selectedOrderKeys += selectedOrders.map { it.selectionKey() }
        _selectedOrderCount.value = selectedOrders.size.toString()
        _selectedOrderAmount.value = if (orders.isEmpty()) {
            "0"
        } else {
            selectedOrders.fold(BigDecimal.ZERO) { total, order ->
                total + (order.actualRepayAmount ?: BigDecimal.ZERO)
            }.formatAmountWithPrefix((selectedOrders.firstOrNull() ?: orders.first()).currencySymbol)
        }
    }

    private fun CatalogItemBean.selectionKey(): String {
        return when {
            !orderNo.isNullOrBlank() -> "orderNo:$orderNo"
            orderId != null -> "orderId:$orderId"
            else -> "productId:$productId"
        }
    }

    val togetherRepayResult = MutableLiveData<CheckoutActionResponse?>()
    fun togetherRepayment(orderList: List<String>) {
        createNetworkRequest {
            repaymentRepository.submitBatchRepayment(orderList)
        }.onSuccess {
            togetherRepayResult.value = it
        }.execute()
    }
}
