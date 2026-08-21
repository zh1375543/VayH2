package com.velora.portal.journey.access.presentation

import android.content.Context
import com.velora.portal.platform.common.data.authConfigList
import com.velora.portal.domain.customer.model.VerificationProgressResponse
import com.velora.portal.journey.access.presentation.contact.PaymentDetailsActivity
import com.velora.portal.journey.access.presentation.kyc.DocumentReviewActivity
import com.velora.portal.journey.access.presentation.profile.ApplicantDetailsActivity
import com.velora.portal.platform.common.util.start

fun VerificationProgressResponse.routeToNextAuthStep(
    context: Context,
    isFromAuthPage: Boolean = true,
) {
    val configList = authConfigList.filterNot { it.isBlank() }
    val hasPassedRequiredSteps = isPass(configList)

    if (userAuthState == "30" && hasPassedRequiredSteps) {
        context.start<ReviewSuccessActivity>()
        return
    }
    if (isFromAuthPage
        && hasPassedRequiredSteps
    ) {
        context.start<ReviewSuccessActivity>()
        return
    }
    configList.forEach {
        when {
            it.uppercase() == "KYC" && kycState != "30" -> {
                context.start<DocumentReviewActivity>()
                return
            }

            it.uppercase() == "ID" && idState != "30" -> {
                context.start<ApplicantDetailsActivity>()
                return
            }

            it.uppercase() == "BANK" && bankCardState != "30" -> {
                context.start<PaymentDetailsActivity>()
                return
            }
        }
    }
}
