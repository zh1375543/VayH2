package com.velora.portal.feature.onboarding.model

data class IdentityDocumentResponse(
    val id: Long,
    val userId: Long,
    val frontImageUrl: String? = null,
    val backImageUrl: String? = null,
    val liveImageUrl: String? = null,
    val idCardType: String? = null,
)
