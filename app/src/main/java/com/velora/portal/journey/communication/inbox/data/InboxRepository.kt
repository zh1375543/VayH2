package com.velora.portal.journey.communication.inbox.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.journey.communication.inbox.model.InboxMessageRecord

class InboxRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchMessages(
        pageNumber: Int = 1,
        pageSize: Int = 9999,
    ): List<InboxMessageRecord> {
        return api.fetchMessageList(
            ApiRequest(
                pageNum = pageNumber,
                pageSize = pageSize,
            )
        ).dataOrThrow()?.list ?: emptyList()
    }

    suspend fun markAsRead(idList: List<Long>): Any? {
        val recordIds = idList
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ",")
        return api.updateMessageStatus(ApiRequest(recordIdStr = recordIds)).dataOrThrow()
    }
}
