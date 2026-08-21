package com.velora.portal.core.common.util

import android.content.Context
import com.velora.portal.R

fun Context.getPayoutAccountTypeLabel(payWay: String?): String = when (payWay) {
    "CARD" -> getString(R.string.bank)
    "WALLET" -> getString(R.string.e_wallet)
    else -> ""
}
