package com.novexa.platform.feature.catalog.presentation.adapter

import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.databinding.ItemApplicationStatusBinding
import com.novexa.platform.feature.catalog.model.CatalogItemBean
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix

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
