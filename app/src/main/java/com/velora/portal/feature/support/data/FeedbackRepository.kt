package com.velora.portal.feature.support.data

import com.velora.portal.core.common.data.bean.ApiRequest
import com.velora.portal.core.network.Api
import com.velora.portal.core.network.NetworkProvider
import com.velora.portal.core.common.data.repository.dataOrThrow

class FeedbackRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun submitFeedback(content: String): Any? {
        return api.submitFeedback(ApiRequest(content = content)).dataOrThrow()
    }
}
