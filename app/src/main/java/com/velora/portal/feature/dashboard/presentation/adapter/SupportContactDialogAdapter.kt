package com.velora.portal.feature.dashboard.presentation.adapter

import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseAdapter
import com.velora.portal.core.common.data.language
import com.velora.portal.databinding.ItemSupportContactBinding
import com.velora.portal.feature.dashboard.model.CustomerContactConfig
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.ExternalActionLauncher
import com.velora.portal.core.common.util.showToastMessage

class SupportContactDialogAdapter :
    BaseAdapter<CustomerContactConfig, ItemSupportContactBinding>(
        ItemSupportContactBinding::inflate) {

    override fun bindItem(
        binding: ItemSupportContactBinding,
        item: CustomerContactConfig,
        position: Int,
    ) = with(binding) {
        tvCopyTelegram.text =
            context.getString(if (item.buttonType == 2) R.string.call else R.string.copy)
        tvTelegram.text = item.content
        tvTelegramTitle.text =
            if (language == "vi") item.vernacularTitle else item.enTitle
        ivTelegram.setImageResource(
            when (tvTelegramTitle.text.toString()) {
                context.getString(R.string.phone_number) -> R.mipmap.icon_phone
                context.getString(R.string.email) -> R.mipmap.icon_email
                else -> R.mipmap.icon_tg
            }
        )
        tvCopyTelegram.singleClick { _ ->
            if (item.buttonType == 2) {
                item.content?.filter { it1 -> it1.isDigit() }?.let {
                    ExternalActionLauncher.openDialer(context, it)
                }
            } else {
                item.content?.let { ExternalActionLauncher.copyText(context, it) }
                context.getString(R.string.copy_success).showToastMessage()
            }
        }
    }
}
