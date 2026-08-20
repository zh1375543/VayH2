package com.novexa.platform.feature.inbox.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.inbox.model.InboxMessageRecord

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
