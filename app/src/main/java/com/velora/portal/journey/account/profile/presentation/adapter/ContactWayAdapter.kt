package com.velora.portal.journey.account.profile.presentation.adapter

import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.platform.common.data.language
import com.velora.portal.databinding.ItemContactWayBinding
import com.velora.portal.journey.lending.dashboard.model.CustomerContactConfig
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.ExternalActionLauncher
import com.velora.portal.platform.common.util.showToastMessage

class ContactWayAdapter :
    BaseAdapter<CustomerContactConfig, ItemContactWayBinding>(ItemContactWayBinding::inflate) {

    override fun bindItem(
        binding: ItemContactWayBinding,
        item: CustomerContactConfig,
        position: Int,
    ) = with(binding) {
        val title = if (language == "vi") item.vernacularTitle else item.enTitle
        tvTelegramTitle.text = title
        tvTelegram.text = item.content

        ivTelegram.setImageResource(
            when (title) {
                context.getString(R.string.phone_number) -> R.mipmap.ic_contact_phone
                context.getString(R.string.email) -> R.mipmap.ic_contact_email
                else -> R.mipmap.ic_contact_tg
            }
        )

        tvCopyTelegram.text =
            context.getString(if (item.buttonType == 2) R.string.call else R.string.copy)

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
