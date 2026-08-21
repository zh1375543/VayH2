package com.velora.portal.feature.catalog.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.databinding.ItemRepaymentOptionBinding
import com.velora.portal.feature.catalog.model.CatalogItemBean
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

class RepaymentPlanOptionAdapter(var selectPosition: Int = 0) :
    BaseAdapter<CatalogItemBean, ItemRepaymentOptionBinding>(ItemRepaymentOptionBinding::inflate) {

    override fun bindItem(
        binding: ItemRepaymentOptionBinding,
        item: CatalogItemBean,
        position: Int,
    ) = with(binding) {
        contentView.isSelected = selectPosition == position
        val isInstall = !item.productInstallmentPlanDTOList.isNullOrEmpty()
        val size = if (isInstall) {
            val list = item.productInstallmentPlanDTOList
            val index = list?.indexOfFirst { it.isDefault == 1 || it.defaultSign == 1 }?.coerceIn(0, list.lastIndex) ?: 0
            list?.get(index)?.appRepaymentPlanDTOList?.size ?: 0
        } else 1
        tvNum1.isSelected = selectPosition == position
        tvNum1.text = context.getString(R.string.phase, size)
        tvNoInstall.isVisible = !isInstall
        firstRepaymentContainer.isVisible = isInstall
        tvFirst.isVisible = isInstall
        tvAmount1.isVisible = isInstall
        tvPeriodDays1.text = item.timeLimit.toString() + context.getString(R.string.days)
        if (isInstall) {
            val list = item.productInstallmentPlanDTOList
            val index = list?.indexOfFirst { it.isDefault == 1 || it.defaultSign == 1 }?.coerceIn(0, list.lastIndex) ?: 0
            tvAmount1.text = list?.get(index)?.firstRepayment.formatAmountWithPrefix()
        }
        tvPeriod1.isSelected = selectPosition == position
        tvPeriodDays1.isSelected = selectPosition == position
        tvFirst.isSelected = selectPosition == position
        tvAmount1.isSelected = selectPosition == position
        tvNoInstall.isSelected = selectPosition == position
    }
}
