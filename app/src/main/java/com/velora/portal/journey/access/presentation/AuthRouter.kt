package com.velora.portal.journey.access.presentation

import android.content.Context
import com.velora.portal.platform.common.data.authConfigList
import com.velora.portal.domain.customer.model.VerificationProgressResponse
import com.velora.portal.journey.access.presentation.contact.ContactPayoutActivity
import com.velora.portal.journey.access.presentation.kyc.IdentityCheckActivity
import com.velora.portal.journey.access.presentation.profile.BorrowerProfileActivity
import com.velora.portal.platform.common.util.start

fun VerificationProgressResponse.routeToNextAuthStep(
    context: Context,
    isFromAuthPage: Boolean = true,
) {
    val configList = authConfigList.filterNot { it.isBlank() }
    val hasPassedRequiredSteps = isPass(configList)

    if (userAuthState == "30" && hasPassedRequiredSteps) {
        context.start<OnboardingCompleteActivity>()
        return
    }
    if (isFromAuthPage
        && hasPassedRequiredSteps
    ) {
        context.start<OnboardingCompleteActivity>()
        return
    }
    configList.forEach {
        when {
            it.uppercase() == "KYC" && kycState != "30" -> {
                context.start<IdentityCheckActivity>()
                return
            }

            it.uppercase() == "ID" && idState != "30" -> {
                context.start<BorrowerProfileActivity>()
                return
            }

            it.uppercase() == "BANK" && bankCardState != "30" -> {
                context.start<ContactPayoutActivity>()
                return
            }
        }
    }
}
