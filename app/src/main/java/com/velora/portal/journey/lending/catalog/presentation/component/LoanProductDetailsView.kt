package com.velora.portal.journey.lending.catalog.presentation.component

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix
import com.velora.portal.databinding.LoanProductDetailsViewBinding
import com.velora.portal.domain.credit.model.CatalogEntry
import com.velora.portal.journey.lending.catalog.presentation.adapter.OfferFeeSummaryAdapter

/** Displays the selected offer's product information and related fees. */
class LoanProductDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    private val binding =
        LoanProductDetailsViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val headerFeeAdapter by lazy { OfferFeeSummaryAdapter() }

    private var hasInstallmentFee = false

    init {
        binding.rvFeeSummary.adapter = headerFeeAdapter
    }

    fun bind(plan: CatalogEntry, currencySymbol: String?) = with(binding) {
        tvDuration.text = context.getString(R.string.num_days, plan.timeLimit.toString())
        tvDisbursedLabel.text =
            String.format(context.getString(R.string.actually_amount), currencySymbol ?: "").replace("()", "")
        tvDisbursedAmount.text = plan.actualAmount.formatAmountWithPrefix(currencySymbol)
        tvInterestLabel.text = context.getString(R.string.interest_day, "${plan.interestRate}%")
        tvInterestAmount.text = plan.interestAmount.formatAmountWithPrefix(currencySymbol)
        tvDueDate.text = plan.repayTimeStr
        hasInstallmentFee = plan.installmentServiceFee?.signum() == 1
        tvServiceFee.text = if (hasInstallmentFee) {
            plan.installmentServiceFee.formatAmountWithPrefix(plan.currencySymbol)
        } else {
            null
        }
        updateInstallmentFeeVisibility()
        tvDeviceModel.text = "${Build.BRAND} ${Build.MODEL}"
        headerFeeAdapter.submitItems(plan.appProductHandleFeeConfigDtos)
    }

    fun setHeaderVisible(visible: Boolean) = with(binding) {
        tvDetailsTitle.isVisible = visible
        ivExpandDetails.isVisible = false
    }

    private fun updateInstallmentFeeVisibility() = with(binding) {
        val shouldShow = hasInstallmentFee
        tvServiceFeeLabel.isVisible = shouldShow
        tvServiceFee.isVisible = shouldShow
    }
}
