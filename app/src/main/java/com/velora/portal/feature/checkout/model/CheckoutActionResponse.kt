package com.velora.portal.feature.checkout.model

data class CheckoutActionResponse(
    val payUrl: String? = null,
    val reloanButtonSign: String? = null,
)
