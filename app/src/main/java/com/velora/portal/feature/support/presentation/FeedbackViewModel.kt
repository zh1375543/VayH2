package com.velora.portal.feature.support.presentation

import com.velora.portal.core.ui.base.BaseViewModel
import com.velora.portal.feature.support.data.FeedbackRepository

/** Submits user feedback independently from the screen that presents the feedback prompt. */
class FeedbackViewModel(
    private val feedbackRepository: FeedbackRepository = FeedbackRepository(),
) : BaseViewModel() {

    fun submitFeed(content: String, action: () -> Unit) {
        createNetworkRequest {
            feedbackRepository.submitFeedback(content)
        }.showLoading().onSuccess {
            action()
        }.execute()
    }
}
