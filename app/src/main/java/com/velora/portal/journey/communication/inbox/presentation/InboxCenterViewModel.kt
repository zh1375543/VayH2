package com.velora.portal.journey.communication.inbox.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.journey.communication.inbox.data.InboxRepository
import com.velora.portal.journey.communication.inbox.model.InboxMessageRecord
import com.velora.portal.platform.common.util.PageLoadState

class InboxCenterViewModel(
    private val messageRepository: InboxRepository = InboxRepository(),
) : BaseViewModel() {

    private val _messageListState = MutableLiveData<PageLoadState<List<InboxMessageRecord>>>(
        PageLoadState.Loading,
    )
    val messageListState: LiveData<PageLoadState<List<InboxMessageRecord>>> = _messageListState

    fun getMessageList() {
        _messageListState.value = PageLoadState.Loading
        createNetworkRequest {
            messageRepository.loadInboxMessages()
        }.onSuccess { result ->
            val messages = result.orEmpty()
            _messageListState.value = if (messages.isEmpty()) {
                PageLoadState.Empty
            } else {
                PageLoadState.Content(messages)
            }
        }.onFailed {
            _messageListState.value = PageLoadState.Error
            false
        }
    }

    fun markMessagesRead(message: InboxMessageRecord) {
        if (message.readStatus) return
        updateReadStatus(listOf(message.id ?: 0L))
    }

    fun markAllAsRead() {
        val unreadIds = currentMessages()
            .filterNot(InboxMessageRecord::readStatus)
            .map { it.id ?: 0L }
        if (unreadIds.isEmpty()) return
        updateReadStatus(unreadIds)
    }

    private fun updateReadStatus(idList: List<Long>) {
        createNetworkRequest {
            messageRepository.markMessagesRead(idList)
        }.showLoading().onSuccess {
            val readIds = idList.toSet()
            val currentState = _messageListState.value
            if (currentState is PageLoadState.Content) {
                _messageListState.value = PageLoadState.Content(
                    currentState.data.map { message ->
                        if ((message.id ?: 0L) in readIds) {
                            message.copy(readStatus = true)
                        } else {
                            message
                        }
                    },
                )
            }
        }.execute()
    }

    private fun currentMessages(): List<InboxMessageRecord> {
        return when (val state = _messageListState.value) {
            is PageLoadState.Content -> state.data
            else -> emptyList()
        }
    }
}
