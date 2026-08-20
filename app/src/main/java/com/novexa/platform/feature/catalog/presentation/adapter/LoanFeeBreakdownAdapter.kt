package com.novexa.platform.feature.catalog.presentation.adapter

import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.feature.catalog.model.CatalogFeeBean
import com.novexa.platform.databinding.ItemProductChargeBinding
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix

class LoanFeeBreakdownAdapter :
    BaseAdapter<CatalogFeeBean, ItemProductChargeBinding>(ItemProductChargeBinding::inflate) {

    override fun bindItem(
        binding: ItemProductChargeBinding,
        item: CatalogFeeBean,
        position: Int,
    ) = with(binding) {
        tvFee.text = item.amount.formatAmountWithPrefix()
        tvTitle.text = item.getFeeName()
    }
}
