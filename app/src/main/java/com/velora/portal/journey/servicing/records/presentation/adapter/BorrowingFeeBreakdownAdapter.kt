package com.velora.portal.journey.servicing.records.presentation.adapter

import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.domain.credit.model.FeeLineItem
import com.velora.portal.databinding.ItemBorrowingFeeBinding
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

class BorrowingFeeBreakdownAdapter :
    BaseAdapter<FeeLineItem, ItemBorrowingFeeBinding>(ItemBorrowingFeeBinding::inflate) {

    var currencySymbol: String? = null

    override fun bindItem(
        binding: ItemBorrowingFeeBinding,
        item: FeeLineItem,
        position: Int,
    ) = with(binding) {
        tvFee.text = item.amount.formatAmountWithPrefix(currencySymbol)
        tvTitle.text = item.getFeeName()
    }
}
