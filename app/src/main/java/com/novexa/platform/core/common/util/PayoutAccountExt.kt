package com.novexa.platform.core.common.util

import android.content.Context
import com.novexa.platform.R

fun Context.getPayoutAccountTypeLabel(payWay: String?): String = when (payWay) {
    "CARD" -> getString(R.string.bank)
    "WALLET" -> getString(R.string.e_wallet)
    else -> ""
}
