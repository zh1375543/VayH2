package com.novexa.platform.calculation.model

import java.math.BigDecimal

/** Response returned from the savings calculation API. */
data class SavingsCalculationResponse(
    val savingsGoal: BigDecimal? = null,
    val currentSavings: BigDecimal? = null,
    val monthlySavings: BigDecimal? = null,
    val monthlyIncome: BigDecimal? = null,
    val remainingAmount: BigDecimal? = null,
    val estimatedCompletionMonths: Int = 0,
    /** Estimated completion date in yyyy-MM-dd format. */
    val estimatedCompletionDate: String? = null,
    /** Monthly savings rate in percentage, e.g. 16.00 means 16%. */
    val monthlySavingsRate: BigDecimal? = null,
    val currencyCode: String? = null,
)
