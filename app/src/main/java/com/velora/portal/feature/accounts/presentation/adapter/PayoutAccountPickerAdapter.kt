package com.velora.portal.feature.accounts.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.feature.accounts.model.LinkedAccountResponse
import com.velora.portal.databinding.ItemAccountAddOptionBinding

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
