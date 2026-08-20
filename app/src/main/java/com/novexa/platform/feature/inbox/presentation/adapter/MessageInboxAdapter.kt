package com.novexa.platform.feature.inbox.presentation.adapter

import androidx.core.view.isVisible
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.databinding.ItemNoticeBinding
import com.novexa.platform.feature.inbox.model.InboxMessageRecord

class MessageInboxAdapter :
    BaseAdapter<InboxMessageRecord, ItemNoticeBinding>(ItemNoticeBinding::inflate) {

    override fun bindItem(
        binding: ItemNoticeBinding,
        item: InboxMessageRecord,
        position: Int,
    ) = with(binding) {
        tvDot.isVisible = !item.readStatus
        tvDate.text = item.getTime()
        tvTitle.text = item.theme
        tvDesc.text = item.content
    }
}
