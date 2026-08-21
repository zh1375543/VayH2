package com.velora.portal.journey.lending.catalog.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.velora.portal.R
import com.velora.portal.domain.credit.model.CatalogItemBean
import com.velora.portal.databinding.ViewOfferSummaryBinding

/** Product and amount summary used on the multiple-loan page. */
class LoanProductSummaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding =
        ViewOfferSummaryBinding.inflate(LayoutInflater.from(context), this, true)

    private var detailsClickAction: (() -> Unit)? = null

    init {
        binding.tvOfferDetails.setOnClickListener { detailsClickAction?.invoke() }
        binding.ivExpandArrow.setOnClickListener { detailsClickAction?.invoke() }
    }

    fun bind(product: CatalogItemBean, displayAmount: CharSequence) = with(binding) {
        tvOfferName.text = product.productName
        tvLoanLabel.text = context.getString(R.string.loan_amount, product.currency)
        tvOfferAmount.text = displayAmount
    }

    fun setExpanded(expanded: Boolean) {
        binding.ivExpandArrow.rotation = if (expanded) 0f else 180f
    }

    fun setOnDetailsClickListener(action: () -> Unit) {
        detailsClickAction = action
    }
}
