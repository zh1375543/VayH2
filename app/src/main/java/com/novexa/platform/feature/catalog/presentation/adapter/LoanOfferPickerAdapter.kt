package com.novexa.platform.feature.catalog.presentation.adapter

import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.feature.catalog.model.CatalogItemBean
import com.novexa.platform.databinding.ItemProductOptionBinding
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix

class LoanOfferPickerAdapter :
    BaseAdapter<CatalogItemBean, ItemProductOptionBinding>(ItemProductOptionBinding::inflate) {

    override fun bindItem(
        binding: ItemProductOptionBinding,
        item: CatalogItemBean,
        position: Int,
    ) = with(binding) {
        tvAmount.text = item.maxLoanAmount.formatAmountWithPrefix(item.currencySymbol)
        tvName.text = item.productName
        tvLoan.text = context.getString(R.string.loan_amount).replace("(%s)", "")
    }
}
