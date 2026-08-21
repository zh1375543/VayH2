package com.velora.portal.journey.lending.catalog.presentation.adapter

import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.databinding.ItemApplicationStatusBinding
import com.velora.portal.domain.credit.model.CatalogItemBean
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

class ApplicationResultAdapter :
    BaseAdapter<CatalogItemBean, ItemApplicationStatusBinding>(ItemApplicationStatusBinding::inflate) {

    override fun bindItem(
        binding: ItemApplicationStatusBinding,
        item: CatalogItemBean,
        position: Int,
    ) = with(binding) {
        tvProductName.text = item.productName
        val a = String.format(context.getString(R.string.loan_amount), item.currency ?: "")
        tvLoanLabel.text = if (item.currency == null) a.replace("()", "") else a
        tvLoanValue.text = item.loanAmount.formatAmountWithPrefix(item.currencySymbol)
        ivVerifyState.isSelected = item.pushStatus == 200
        tvProductName.isSelected = item.pushStatus == 200
    }
}
