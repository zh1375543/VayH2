package com.velora.portal.moneyflow.model

import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.data.APPCODE
import java.math.BigDecimal

/** Request parameters for bonus / 13th-month pay calculation. */
data class BonusCalculationRequest(
    /** Monthly basic salary, must be greater than 0. */
    val monthlyBasicSalary: BigDecimal,
    /** Monthly bonus amount, must be >= 0. */
    val bonusAmount: BigDecimal,
    /** Bonus type: THIRTEENTH_MONTH, PERFORMANCE, ANNUAL, or OTHER. */
    val bonusType: String,
    /** Tax year, range 2000 to current year. */
    val taxYear: Int,
    val version: String? = BuildConfig.VERSION_NAME,
    val mobileType: String = "2",
    val appCode: String = APPCODE,
)
