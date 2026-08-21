package com.velora.portal.journey.communication.inbox.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.journey.communication.inbox.model.InboxMessageRecord

class InboxRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadInboxMessages(
        pageNumber: Int = 1,
        pageSize: Int = 9999,
    ): List<InboxMessageRecord> {
        return api.loadInboxMessages(
            ApiRequest(
                pageNum = pageNumber,
                pageSize = pageSize,
            )
        ).dataOrThrow()?.list ?: emptyList()
    }

    suspend fun markMessagesRead(idList: List<Long>): Any? {
        val recordIds = idList
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ",")
        return api.markMessageRead(ApiRequest(recordIdStr = recordIds)).dataOrThrow()
    }
}
