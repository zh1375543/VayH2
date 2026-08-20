package com.novexa.platform.feature.profile.presentation.adapter

import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.databinding.ItemAuthEntryBinding
import com.novexa.platform.feature.onboarding.model.VerificationOptionResponse

class VerificationRequirementAdapter :
    BaseAdapter<VerificationOptionResponse, ItemAuthEntryBinding>(ItemAuthEntryBinding::inflate) {

    override fun bindItem(
        binding: ItemAuthEntryBinding,
        item: VerificationOptionResponse,
        position: Int,
    ) = with(binding) {
        ivIcon.setImageResource(item.src)
        tvTitle.text = item.title
        tvTitle.isSelected = item.isCertified
    }
}
