package com.velora.portal.moneyflow.model

import java.math.BigDecimal

/** Response returned after setting the user's salary configuration. */
data class SetSalaryResponse(
    val profileConfigured: Boolean = false,
    val monthlySalary: BigDecimal? = null,
    val workingDays: Int = 0,
    val workHoursPerDay: BigDecimal? = null,
    val paydayDay: Int = 0,
    val workLocation: String? = null,
    val currencyCode: String? = null,
    val currencySymbol: String? = null,
    val dailyWage: BigDecimal? = null,
    val hourlyWage: BigDecimal? = null,
    val nextPaydayDate: String? = null,
)
