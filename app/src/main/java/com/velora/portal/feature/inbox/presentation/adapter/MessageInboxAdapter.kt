package com.velora.portal.feature.inbox.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.databinding.ItemNoticeBinding
import com.velora.portal.feature.inbox.model.InboxMessageRecord

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
