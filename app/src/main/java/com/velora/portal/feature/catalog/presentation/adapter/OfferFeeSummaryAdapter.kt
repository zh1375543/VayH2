package com.velora.portal.feature.catalog.presentation.adapter

import com.velora.portal.core.ui.base.BaseAdapter
import com.velora.portal.databinding.ItemOfferFeeSummaryBinding
import com.velora.portal.feature.catalog.model.CatalogFeeBean
import com.velora.portal.core.common.util.text.formatAmountWithPrefix

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
