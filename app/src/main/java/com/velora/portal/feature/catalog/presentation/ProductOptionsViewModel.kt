package com.velora.portal.feature.catalog.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_LoanAppProductInfoDetail
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.feature.catalog.data.ProductOptionsRepository
import com.velora.portal.feature.catalog.model.CatalogItemBean
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.PageLoadState

class ProductOptionsViewModel(
    private val loanProductRepository: ProductOptionsRepository = ProductOptionsRepository(),
) : BaseViewModel() {

    private val _productDetailState = MutableLiveData<PageLoadState<CatalogItemBean>>(
        PageLoadState.Loading,
    )
    val productDetailState: LiveData<PageLoadState<CatalogItemBean>> = _productDetailState
    val detailResult: LiveData<CatalogItemBean?> = MediatorLiveData<CatalogItemBean?>().apply {
        addSource(_productDetailState) { state ->
            if (state is PageLoadState.Content) {
                value = state.data
            }
        }
    }

    fun showProductDetail(product: CatalogItemBean) {
        _productDetailState.value = PageLoadState.Content(product)
    }

    fun getProductDetail(
        trackPage: String,
        id: String?,
        amount: String?,
        showLoading: Boolean = false,
        errorAction: () -> Unit,
    ) {
        _productDetailState.value = PageLoadState.Loading
        createNetworkRequest {
            loanProductRepository.fetchProductDetail(
                productId = id,
                amount = amount,
            )
        }.showLoading(showLoading).onSuccess { product ->
            submitTrackingEvent(
                TrackBean(
                    p = trackPage,
                    act = ACT_LoanAppProductInfoDetail,
                    result = product.toJsonString()
                )
            )
            _productDetailState.value = if (product == null) {
                PageLoadState.Error
            } else {
                PageLoadState.Content(product)
            }
        }.onFailed {
            submitTrackingEvent(
                TrackBean(
                    p = trackPage,
                    act = ACT_LoanAppProductInfoDetail,
                    result = it.toJsonString()
                )
            )
            errorAction.invoke()
            _productDetailState.value = PageLoadState.Error
            false
        }
    }
}
