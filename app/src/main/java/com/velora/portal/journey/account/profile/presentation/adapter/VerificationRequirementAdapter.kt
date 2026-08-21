package com.velora.portal.journey.account.profile.presentation.adapter

import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.databinding.ItemAuthEntryBinding
import com.velora.portal.domain.customer.model.VerificationOptionResponse

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
