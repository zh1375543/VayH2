package com.velora.portal.calculation.model

import com.velora.portal.BuildConfig
import com.velora.portal.core.common.data.APPCODE
import java.math.BigDecimal

/**
 * Request parameters for salary calculation.
 *
 * When [useSavedSalary] is true, the server reads the user's saved salary config;
 * when false, [monthlySalary], [workingDays] and [workHoursPerDay] must be provided.
 */
data class SalaryCalculationRequest(
    val useSavedSalary: Boolean,
    val monthlySalary: BigDecimal? = null,
    val workingDays: Int? = null,
    val workHoursPerDay: BigDecimal? = null,
    val version: String? = BuildConfig.VERSION_NAME,
    val mobileType: String = "2",
    val appCode: String = APPCODE,
)