package com.novexa.platform.feature.catalog.presentation.adapter

import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.databinding.ItemOfferFeeSummaryBinding
import com.novexa.platform.feature.catalog.model.CatalogFeeBean
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix

class OfferFeeSummaryAdapter :
    BaseAdapter<CatalogFeeBean, ItemOfferFeeSummaryBinding>(ItemOfferFeeSummaryBinding::inflate) {

    override fun bindItem(
        binding: ItemOfferFeeSummaryBinding,
        item: CatalogFeeBean,
        position: Int,
    ) = with(binding) {
        tvTitle.text = item.getFeeName()
        tvFee.text = item.amount.formatAmountWithPrefix()
    }
}
