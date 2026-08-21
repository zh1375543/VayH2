package com.velora.portal.journey.lending.catalog.presentation.adapter

import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.domain.credit.model.FeeLineItem
import com.velora.portal.databinding.ItemProductChargeBinding
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

class LoanFeeBreakdownAdapter :
    BaseAdapter<FeeLineItem, ItemProductChargeBinding>(ItemProductChargeBinding::inflate) {

    override fun bindItem(
        binding: ItemProductChargeBinding,
        item: FeeLineItem,
        position: Int,
    ) = with(binding) {
        tvFee.text = item.amount.formatAmountWithPrefix()
        tvTitle.text = item.getFeeName()
    }
}
