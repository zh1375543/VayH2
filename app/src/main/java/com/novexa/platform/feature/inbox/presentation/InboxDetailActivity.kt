package com.novexa.platform.feature.inbox.presentation

import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityInboxDetailBinding
import com.novexa.platform.feature.inbox.model.InboxMessageRecord
import com.novexa.platform.core.common.util.viewBinding

class InboxDetailActivity :
    BaseActivity<ActivityInboxDetailBinding>() {

    override val binding by viewBinding(ActivityInboxDetailBinding::inflate)
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
