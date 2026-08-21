package com.velora.portal.journey.communication.support.presentation

import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.journey.communication.support.data.FeedbackRepository

/** Submits user feedback independently from the screen that presents the feedback prompt. */
class FeedbackViewModel(
    private val feedbackRepository: FeedbackRepository = FeedbackRepository(),
) : BaseViewModel() {

    fun submitFeed(content: String, action: () -> Unit) {
        createNetworkRequest {
            feedbackRepository.sendRatingFeedback(content)
        }.showLoading().onSuccess {
            action()
        }.execute()
    }
}
