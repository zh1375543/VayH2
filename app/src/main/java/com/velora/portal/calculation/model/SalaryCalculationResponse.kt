package com.velora.portal.calculation.model

import java.math.BigDecimal

/** Response returned from the salary calculation API. */
data class SalaryCalculationResponse(
    val monthlySalary: BigDecimal? = null,
    val workingDays: Int = 0,
    val workHoursPerDay: BigDecimal? = null,
    val dailyWage: BigDecimal? = null,
    val hourlyWage: BigDecimal? = null,
    val currencyCode: String? = null,
)