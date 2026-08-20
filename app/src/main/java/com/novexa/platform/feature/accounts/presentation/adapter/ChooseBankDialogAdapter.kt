package com.novexa.platform.feature.accounts.presentation.adapter

import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.feature.accounts.model.AccountChannelResponse
import com.novexa.platform.databinding.ItemBankOptionBinding
import com.novexa.platform.core.ui.extension.loadImage
import androidx.core.view.isVisible

class ChooseBankDialogAdapter :
    BaseAdapter<AccountChannelResponse, ItemBankOptionBinding>(ItemBankOptionBinding::inflate) {

    override fun bindItem(
        binding: ItemBankOptionBinding,
        item: AccountChannelResponse,
        position: Int,
    ) = with(binding) {
        ivBank.loadImage(item.logoUrl, R.mipmap.ic_bank_header)
        tvTitle.text = item.longCode
        tvContent.text = item.bankName
        divider.isVisible = position < items.lastIndex
    }
}
