package com.velora.portal.feature.onboarding.model

data class AccessSessionResponse(
    val token: String,
    val id: Long,
    val phone: String,
    val appId: Long?,
    val channelId: Long?,
    val passwdSign: Int,
    val activityUrl: String? = null,
)
