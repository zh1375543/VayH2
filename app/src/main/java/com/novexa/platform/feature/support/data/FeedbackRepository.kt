package com.novexa.platform.feature.support.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow

class FeedbackRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun submitFeedback(content: String): Any? {
        return api.submitFeedback(ApiRequest(content = content)).dataOrThrow()
    }
}
