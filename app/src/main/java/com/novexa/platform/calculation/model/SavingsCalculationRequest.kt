package com.novexa.platform.calculation.model

import com.novexa.platform.BuildConfig
import com.novexa.platform.core.common.data.APPCODE
import java.math.BigDecimal

/** Request parameters for savings calculation. */
data class SavingsCalculationRequest(
    /** Savings goal, must be greater than 0. */
    val savingsGoal: BigDecimal,
    /** Current savings, must be >= 0. */
    val currentSavings: BigDecimal,
    /** Monthly savings amount, must be >= 0. */
    val monthlySavings: BigDecimal,
    /** Monthly income, must be >= 0. */
    val monthlyIncome: BigDecimal,
    val version: String? = BuildConfig.VERSION_NAME,
    val mobileType: String = "2",
    val appCode: String = APPCODE,
)
