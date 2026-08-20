package com.novexa.platform.feature.accounts.presentation.adapter

import androidx.core.view.isVisible
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.feature.accounts.model.AccountMethodResponse
import com.novexa.platform.databinding.ItemWalletOptionBinding

class ChooseWalletDialogAdapter :
    BaseAdapter<AccountMethodResponse, ItemWalletOptionBinding>(ItemWalletOptionBinding::inflate) {

    override fun bindItem(
        binding: ItemWalletOptionBinding,
        item: AccountMethodResponse,
        position: Int,
    ) = with(binding) {
        tvTitle.text = item.walletName.orEmpty()
        divider.isVisible = position < items.lastIndex
    }
}
