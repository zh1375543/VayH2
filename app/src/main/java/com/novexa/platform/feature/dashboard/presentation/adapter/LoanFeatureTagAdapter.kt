package com.novexa.platform.feature.dashboard.presentation.adapter

import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.databinding.ItemHomeTagBinding

class LoanFeatureTagAdapter : BaseAdapter<String, ItemHomeTagBinding>(ItemHomeTagBinding::inflate) {

    override fun bindItem(
        binding: ItemHomeTagBinding,
        item: String,
        position: Int,
    ) = with(binding) {
        tvTitle.text = item
        tvTitle.isSelected = position % 2 == 0
    }
}
