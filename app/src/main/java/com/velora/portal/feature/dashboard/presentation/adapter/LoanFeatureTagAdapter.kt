package com.velora.portal.feature.dashboard.presentation.adapter

import com.velora.portal.core.ui.base.BaseAdapter
import com.velora.portal.databinding.ItemHomeTagBinding

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
