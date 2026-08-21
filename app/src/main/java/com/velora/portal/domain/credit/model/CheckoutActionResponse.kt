package com.velora.portal.domain.credit.model

data class CheckoutActionResponse(
    val payUrl: String? = null,
    val reloanButtonSign: String? = null,
)
