package com.velora.portal.journey.lending.catalog.presentation.adapter

import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.domain.credit.model.CatalogItemBean
import com.velora.portal.databinding.ItemProductOptionBinding
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

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
