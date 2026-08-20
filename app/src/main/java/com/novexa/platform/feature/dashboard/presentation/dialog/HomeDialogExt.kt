package com.novexa.platform.feature.dashboard.presentation.dialog

import android.content.Context
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseDialog
import com.novexa.platform.core.ui.extension.setSpannableClickableText
import com.novexa.platform.databinding.ContactUsDialogBinding
import com.novexa.platform.databinding.DialogApplicationRejectedBinding
import com.novexa.platform.databinding.DialogOfferExpiredBinding
import com.novexa.platform.feature.dashboard.model.CustomerContactConfig
import com.novexa.platform.feature.dashboard.model.VisitorPortalResponse
import com.novexa.platform.feature.dashboard.presentation.adapter.SupportContactDialogAdapter
import com.novexa.platform.core.common.util.context.resolveColorCompat

fun Context.showContactUsDialog(homeBean: VisitorPortalResponse) {
    object : BaseDialog<ContactUsDialogBinding>(
        this,
        ContactUsDialogBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            val list = mutableListOf<CustomerContactConfig>()
            homeBean.customerPhone?.let { phone ->
                list.add(
                    CustomerContactConfig(
                        enTitle = "Phone Number",
                        vernacularTitle = "Số điện thoại",
                        content = phone,
                        buttonType = 2
                    )
                )
            }
            homeBean.customerEmail?.let { email ->
                list.add(
                    CustomerContactConfig(
                        enTitle = "Email",
                        vernacularTitle = "Email",
                        content = email,
                        buttonType = 1
                    )
                )
            }
            homeBean.customerConfigs?.let { configs ->
                list.addAll(configs)
            }
            rvCustomer.adapter = SupportContactDialogAdapter().apply {
                submitItems(list)
            }
        }
    }.show()
}



fun Context.showPreCreditExpiredDialog(date: String) {
    object : BaseDialog<DialogOfferExpiredBinding>(
        this,
        DialogOfferExpiredBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            tvExpirationMessage.setSpannableClickableText(
                String.format(getString(R.string.pre_credit_has_expired_tips), date),
                date.ifBlank { "XXXXXXXX" },
                resolveColorCompat(R.color.text_primary)
            ) {
            }
        }
    }.show()
}

fun Context.showCreditUnderReviewDialog() {
    object : BaseDialog<DialogApplicationRejectedBinding>(
        this,
        DialogApplicationRejectedBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
        }
    }.show()
}
