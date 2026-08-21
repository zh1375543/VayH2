package com.velora.portal.feature.onboarding.model

data class RegionalDirectoryResponse(
    val id: Int,
    val parentId: Long,
    val name: String? = null,
    val otherName: String? = null,
    val type: Int,
    val countryId: Int,
)
