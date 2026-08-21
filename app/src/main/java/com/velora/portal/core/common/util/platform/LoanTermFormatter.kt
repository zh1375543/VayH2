package com.velora.portal.core.common.util.platform

import android.content.Context
import com.velora.portal.R

fun Context.formatLoanTerm(value: Any?): String {
    val text = value?.toString()?.trim().orEmpty()
    if (text.isEmpty()) return ""

    val dayLabel = getString(R.string.days)
    return if (text.endsWith(dayLabel, ignoreCase = true)) text else getString(R.string.num_days, text)
}
