package com.velora.portal.feature.catalog.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.velora.portal.databinding.ViewProductDetailBinding
import com.velora.portal.feature.catalog.model.CatalogItemBean

class SingleProductDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding =
        ViewProductDetailBinding.inflate(LayoutInflater.from(context), this, true)

    var onTermChanged: ((productId: Long?, termId: Long?) -> Unit)?
        get() = binding.repaymentPlanView.onTermChanged
        set(value) {
            binding.repaymentPlanView.onTermChanged = value
        }

    var onInstallmentChanged: ((productId: Long?, planNum: Int?) -> Unit)?
        get() = binding.repaymentPlanView.onInstallmentChanged
        set(value) {
            binding.repaymentPlanView.onInstallmentChanged = value
        }

    var onPlanSelected: ((CatalogItemBean) -> Unit)? = null

    init {
        binding.productDetailsView.setHeaderVisible(false)
        binding.repaymentPlanView.onPlanSelected = { plan -> onPlanSelected?.invoke(plan) }
    }

    fun setData(product: CatalogItemBean) {
        binding.repaymentPlanView.setData(product)
    }

    fun bindHeaderDetail(plan: CatalogItemBean, currencySymbol: String?) {
        binding.productDetailsView.bind(plan, currencySymbol)
    }
}
