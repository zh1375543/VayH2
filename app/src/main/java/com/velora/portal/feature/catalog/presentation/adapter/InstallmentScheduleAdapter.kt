package com.velora.portal.feature.catalog.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.core.ui.base.BaseAdapter
import com.velora.portal.databinding.ItemInstallmentScheduleBinding
import com.velora.portal.feature.catalog.model.CatalogPlanBean
import com.velora.portal.feature.catalog.presentation.ProductOptionsActivity
import com.velora.portal.core.common.util.text.formatAmountWithPrefix

class InstallmentScheduleAdapter :
    BaseAdapter<CatalogPlanBean, ItemInstallmentScheduleBinding>(ItemInstallmentScheduleBinding::inflate) {

    override fun bindItem(
        binding: ItemInstallmentScheduleBinding,
        item: CatalogPlanBean,
        position: Int,
    ) = with(binding) {
        tvDueDate.text = item.repayTime?.substringBefore(" ")
        tvAmount.text = item.totalRepayment.formatAmountWithPrefix()
        ivArrow.rotation = 180f
        infoLayout.isVisible = false
        val toggleDetails = {
            infoLayout.isVisible = !infoLayout.isVisible
            ivArrow.rotation = if (infoLayout.isVisible) 0f else 180f
            if (context is ProductOptionsActivity) {
                (context as ProductOptionsActivity?)?.scrollToOfferActions()
            }
        }
        tvDueDate.setOnClickListener { toggleDetails() }
        tvAmount.setOnClickListener { toggleDetails() }
        ivArrow.setOnClickListener { toggleDetails() }
        tvLoanAmount.text = item.repayActualAmount.formatAmountWithPrefix()
        tvInterest.text = item.repayInterestAmount.formatAmountWithPrefix()
        tvServiceFee.text = item.repayAfterHandleAmount.formatAmountWithPrefix()
    }
}
