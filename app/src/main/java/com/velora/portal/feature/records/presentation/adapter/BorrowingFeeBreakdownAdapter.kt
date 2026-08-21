package com.velora.portal.feature.records.presentation.adapter

import com.velora.portal.core.ui.base.BaseAdapter
import com.velora.portal.feature.catalog.model.CatalogFeeBean
import com.velora.portal.databinding.ItemBorrowingFeeBinding
import com.velora.portal.core.common.util.text.formatAmountWithPrefix

class BorrowingFeeBreakdownAdapter :
    BaseAdapter<CatalogFeeBean, ItemBorrowingFeeBinding>(ItemBorrowingFeeBinding::inflate) {

    var currencySymbol: String? = null

    override fun bindItem(
        binding: ItemBorrowingFeeBinding,
        item: CatalogFeeBean,
        position: Int,
    ) = with(binding) {
        tvFee.text = item.amount.formatAmountWithPrefix(currencySymbol)
        tvTitle.text = item.getFeeName()
    }
}
