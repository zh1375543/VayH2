package com.novexa.platform.feature.accounts.presentation.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.novexa.platform.R
import com.novexa.platform.core.common.util.context.resolveColorCompat
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.core.ui.component.StatefulActionButton
import com.novexa.platform.databinding.ItemBankCardBinding
import com.novexa.platform.feature.accounts.model.LinkedAccountResponse
import com.novexa.platform.feature.accounts.presentation.binding.bindAccountIcon

class PayoutAccountAdapter :
    BaseAdapter<LinkedAccountResponse, ItemBankCardBinding>(ItemBankCardBinding::inflate) {

    override fun bindItem(
        binding: ItemBankCardBinding,
        item: LinkedAccountResponse,
        position: Int,
    ) = with(binding) {
        ivAccountIcon.bindAccountIcon(item.payWay)
        tvBankName.text = item.bankName
        tvBankCard.text = item.bankNo
        val isDefault = item.isDefault == 1
        val accentColor = root.context.resolveColorCompat(
            if (item.payWay == "WALLET") R.color.action_withdraw else R.color.brand_primary,
        )
        root.setBackgroundResource(
            if (item.payWay == "WALLET") R.mipmap.icon_account_wallet else R.mipmap.icon_account_bank,
        )
        menuGroup.isVisible = !isDefault
        tvDefaultStatus.isVisible = isDefault
        tvDelete.updateAppearance(
            variant = StatefulActionButton.VARIANT_FILLED,
            solidColor = root.context.resolveColorCompat(R.color.text_inverse),
            strokeColor = root.context.resolveColorCompat(R.color.text_inverse),
            textColor = accentColor,
        )
        tvDefault.updateAppearance(
            variant = StatefulActionButton.VARIANT_FILLED,
            solidColor = accentColor,
            textColor = root.context.resolveColorCompat(R.color.text_inverse),
        )
    }

    override fun bindChildClickListeners(
        binding: ItemBankCardBinding,
        item: LinkedAccountResponse,
        position: Int,
    ) = with(binding) {
        super.bindChildClickListeners(binding, item, position)
        listOf(tvDelete, tvDefault).forEach { view ->
            view.setOnClickListener {
                dispatchCurrentChildClick(it, binding)
            }
        }
    }

    private fun dispatchCurrentChildClick(view: View, binding: ItemBankCardBinding) {
        val recyclerView = binding.root.parent as? RecyclerView ?: return
        val currentPosition = recyclerView.getChildAdapterPosition(binding.root)
        val currentItem = items.getOrNull(currentPosition) ?: return
        dispatchChildClick(view, currentItem, currentPosition)
    }
}
