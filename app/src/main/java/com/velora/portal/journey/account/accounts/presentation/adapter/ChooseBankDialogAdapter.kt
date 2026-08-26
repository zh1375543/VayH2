package com.velora.portal.journey.account.accounts.presentation.adapter

import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.domain.payout.model.AccountChannelResponse
import com.velora.portal.databinding.ItemBankOptionBinding
import com.velora.portal.platform.design.extension.loadImage
import androidx.core.view.isVisible

class ChooseBankDialogAdapter :
    BaseAdapter<AccountChannelResponse, ItemBankOptionBinding>(ItemBankOptionBinding::inflate) {

    override fun bindItem(
        binding: ItemBankOptionBinding,
        item: AccountChannelResponse,
        position: Int,
    ) = with(binding) {
        ivBank.loadImage(item.logoUrl, R.mipmap.ic_bank_default)
        tvTitle.text = item.longCode
        tvContent.text = item.bankName
        divider.isVisible = position < items.lastIndex
    }
}
