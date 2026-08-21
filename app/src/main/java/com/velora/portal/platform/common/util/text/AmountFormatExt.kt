package com.velora.portal.platform.common.util.text

import java.math.BigDecimal
import java.text.DecimalFormat

fun BigDecimal?.formatAmountWithPrefix(symbol: String? = "₱"): String {
    val formatter = DecimalFormat("#,###.##")
    return (symbol ?: "₱") + formatter.format(this ?: 0)
}
