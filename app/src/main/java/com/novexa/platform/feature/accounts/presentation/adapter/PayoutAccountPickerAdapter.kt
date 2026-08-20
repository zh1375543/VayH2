package com.novexa.platform.feature.accounts.presentation.adapter

import androidx.core.view.isVisible
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.feature.accounts.model.LinkedAccountResponse
import com.novexa.platform.databinding.ItemAccountAddOptionBinding

class PayoutAccountPickerAdapter(var selectPosition: Int) :
    BaseAdapter<LinkedAccountResponse, ItemAccountAddOptionBinding>(ItemAccountAddOptionBinding::inflate) {

    override fun bindItem(
        binding: ItemAccountAddOptionBinding,
        item: LinkedAccountResponse,
        position: Int,
    ) = with(binding) {
        val isWallet = item.payWay == "WALLET"
        val isSelected = selectPosition == position

        tvCard.text = item.account ?: item.bankNo
        tvBank.text = item.name ?: item.bankName
        ivAccountIcon.setImageResource(
            if (isSelected) R.mipmap.icon_dialog_bank_select else R.mipmap.icon_dialog_bank_unselect
        )
        ivBankState.isVisible = !isWallet
        accountItemBackground.isSelected = isSelected
        ivAccountSelectState.isVisible = isSelected
        ivAccountSelectState.setImageResource(R.mipmap.icon_default_checked)
    }
}
