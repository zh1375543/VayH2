package com.novexa.platform.feature.records.presentation.adapter

import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.feature.catalog.model.CatalogFeeBean
import com.novexa.platform.databinding.ItemBorrowingFeeBinding
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix

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
