package com.velora.portal.moneyflow.model

import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.data.APPCODE
import java.math.BigDecimal

/** Request parameters for tax calculation. */
data class TaxCalculationRequest(
    /** Income period: MONTHLY, QUARTERLY, or YEARLY. */
    val incomePeriod: String,
    /** Gross income for the current period. */
    val grossIncome: BigDecimal,
    /** Additional income. Defaults to 0. */
    val additionalIncome: BigDecimal? = null,
    /** Deduction amount. Defaults to 0. */
    val deductions: BigDecimal? = null,
    val version: String? = BuildConfig.VERSION_NAME,
    val mobileType: String = "2",
    val appCode: String = APPCODE,
)
