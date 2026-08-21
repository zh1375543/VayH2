package com.velora.portal.journey.communication.inbox.presentation

import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenMessageDetailBinding
import com.velora.portal.journey.communication.inbox.model.InboxMessageRecord
import com.velora.portal.platform.common.util.viewBinding

class MessageDetailActivity :
    BaseActivity<ScreenMessageDetailBinding>() {

    override val binding by viewBinding(ScreenMessageDetailBinding::inflate)
    private val messageRecord by lazy { intent.getParcelableExtra<InboxMessageRecord>("msg") }

    override fun initView() {
        with(binding) {
            messageRecord?.let { message ->
                tvTitle.text = message.theme
                tvDate.text = message.getTime()
                tvContent.text = message.content
            }
        }
    }
}
