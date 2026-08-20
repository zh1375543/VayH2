package com.novexa.platform.calculation.model

import java.math.BigDecimal

data class CalculationHomeResponse(
    val profileConfigured: Boolean = false,
    val greeting: String? = null,
    val monthlySalary: BigDecimal? = null,
    val dailySalary: BigDecimal? = null,
    val hourlySalary: BigDecimal? = null,
    val todayEarnings: BigDecimal? = null,
    val todayEarningsEstimated: Boolean = false,
    val nextPayday: NextPayday? = null,
    val incomeComparison: IncomeComparison? = null,
    val monthlyGoal: MonthlyGoal? = null,
)

data class NextPayday(
    val date: String? = null,
    val daysLeft: Int = 0,
)

data class IncomeComparison(
    val percentile: Int = 0,
    val workLocation: String? = null,
    val message: String? = null,
)

data class MonthlyGoal(
    val configured: Boolean = false,
    val savingsGoal: BigDecimal? = null,
    val currentSavings: BigDecimal? = null,
    val monthlySavings: BigDecimal? = null,
    val remainingAmount: BigDecimal? = null,
    val progress: BigDecimal? = null,
    val estimatedCompletionMonths: Int = 0,
    val monthlySavingsRate: BigDecimal? = null,
)