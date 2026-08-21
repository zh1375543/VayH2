package com.velora.portal.feature.catalog.presentation.adapter

import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.feature.catalog.model.CatalogFeeBean
import com.velora.portal.databinding.ItemProductChargeBinding
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

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
