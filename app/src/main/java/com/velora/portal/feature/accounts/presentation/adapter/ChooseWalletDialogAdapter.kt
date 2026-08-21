package com.velora.portal.feature.accounts.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.core.ui.base.BaseAdapter
import com.velora.portal.feature.accounts.model.AccountMethodResponse
import com.velora.portal.databinding.ItemWalletOptionBinding

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
