package com.velora.portal.journey.communication.support.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow

class FeedbackRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun submitFeedback(content: String): Any? {
        return api.submitFeedback(ApiRequest(content = content)).dataOrThrow()
    }
}
