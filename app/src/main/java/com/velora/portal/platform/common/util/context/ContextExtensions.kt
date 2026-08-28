package com.velora.portal.platform.common.util.context

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.velora.portal.R
import com.velora.portal.journey.access.presentation.login.PhoneAuthActivity
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.session.SessionStore

fun Context.resolveColorCompat(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Context.getPayoutAccountTypeLabel(payWay: String?): String = when (payWay) {
    "CARD" -> getString(R.string.bank)
    "WALLET" -> getString(R.string.e_wallet)
    else -> ""
}

fun Context.formatLoanTerm(value: Any?): String {
    val text = value?.toString()?.trim().orEmpty()
    if (text.isEmpty()) return ""

    val dayLabel = getString(R.string.days)
    return if (text.endsWith(dayLabel, ignoreCase = true)) text else getString(R.string.num_days, text)
}

fun Context.requireLogin(whenLoggedIn: () -> Unit) {
    if (SessionStore.isLoggedIn) {
        whenLoggedIn()
    } else {
        start<PhoneAuthActivity>()
    }
}
