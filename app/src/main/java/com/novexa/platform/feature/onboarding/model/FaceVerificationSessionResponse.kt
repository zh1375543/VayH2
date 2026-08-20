package com.novexa.platform.feature.onboarding.model

data class FaceVerificationSessionResponse(
    val verifyUrl: String? = null,
    val bizNo: String? = null,
    val expiredTime: Long? = null,
    val faceUrl: String? = null,
)
