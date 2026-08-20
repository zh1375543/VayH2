package com.novexa.platform.calculation.model

import com.novexa.platform.BuildConfig
import com.novexa.platform.core.common.data.APPCODE
import java.math.BigDecimal

/**
 * Request parameters for work hours / earnings calculation.
 *
 * Salary-related fields ([monthlySalary], [workingDays], [workHoursPerDay])
 * are required when [useSavedSalary] is false.
 */
data class WorkHoursCalculationRequest(
    /** Start time in HH:mm format, minute must be 00 or 30. */
    val startTime: String,
    /** End time in HH:mm format, minute must be 00 or 30. */
    val endTime: String,
    /** Break duration in minutes. Allowed values: 0, 30, 60, 90, 120, 150, 180. */
    val breakMinutes: Int,
    /** Whether to use the saved salary configuration. */
    val useSavedSalary: Boolean,
    /** Monthly salary. Required when [useSavedSalary] is false. */
    val monthlySalary: BigDecimal? = null,
    /** Working days per month. Required when [useSavedSalary] is false. */
    val workingDays: Int? = null,
    /** Working hours per day. Required when [useSavedSalary] is false. */
    val workHoursPerDay: BigDecimal? = null,
    val version: String? = BuildConfig.VERSION_NAME,
    val mobileType: String = "2",
    val appCode: String = APPCODE,
)