package com.velora.portal.domain.credit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class FeeLineItem(
    val productId: Long,
    val name: String? = null,
    val nameConfig: String? = null,
    val amount: BigDecimal? = null,
) : Parcelable {
    fun getFeeName(): String? = name
}
