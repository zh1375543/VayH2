package com.novexa.platform.feature.onboarding.model

data class VerificationOptionResponse(
    var src: Int = 0,
    var title: String = "",
    var type: String = "",
    var isCertified: Boolean = false,
    val authConfig: String? = "",
)
