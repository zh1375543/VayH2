package com.velora.portal.calculation.model

import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.data.APPCODE
import java.math.BigDecimal

/** Request parameters used to save the calculation home savings summary. */
data class CalculationHomeRequest(
    val savingsGoal: BigDecimal? = null,
    val currentSavings: BigDecimal? = null,
    val monthlySavings: BigDecimal? = null,
    val version: String? = BuildConfig.VERSION_NAME,
    val mobileType: String = "2",
    val appCode: String = APPCODE,

)
