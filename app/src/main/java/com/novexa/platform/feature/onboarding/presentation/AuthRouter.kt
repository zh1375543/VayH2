package com.novexa.platform.feature.onboarding.presentation

import android.content.Context
import com.novexa.platform.core.common.data.authConfigList
import com.novexa.platform.feature.onboarding.model.VerificationProgressResponse
import com.novexa.platform.feature.onboarding.presentation.contact.PaymentDetailsActivity
import com.novexa.platform.feature.onboarding.presentation.kyc.DocumentReviewActivity
import com.novexa.platform.feature.onboarding.presentation.profile.ApplicantDetailsActivity
import com.novexa.platform.core.common.util.start

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
