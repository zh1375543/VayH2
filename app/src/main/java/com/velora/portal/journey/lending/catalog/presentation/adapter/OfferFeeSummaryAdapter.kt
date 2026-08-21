package com.velora.portal.journey.lending.catalog.presentation.adapter

import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.databinding.ItemOfferFeeSummaryBinding
import com.velora.portal.domain.credit.model.CatalogFeeBean
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

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
