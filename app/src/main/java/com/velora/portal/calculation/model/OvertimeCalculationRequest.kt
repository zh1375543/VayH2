package com.velora.portal.calculation.model

import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.data.APPCODE
import java.math.BigDecimal

/**
 * Request parameters for overtime pay calculation.
 *
 * [overtimeType] and [overtimeMultiplier] are conditionally required —
 * at least one must be provided. When [overtimeMultiplier] is set, it takes priority.
 *
 * Salary-related fields ([monthlySalary], [workingDays], [workHoursPerDay])
 * are required when [useSavedSalary] is false.
 */
data class OvertimeCalculationRequest(
    /** Overtime type identifier. Required when [overtimeMultiplier] is not provided. */
    val overtimeType: String? = null,
    /** Custom overtime multiplier, range (0, 10]. Takes priority over [overtimeType]. */
    val overtimeMultiplier: BigDecimal? = null,
    /** Overtime hours, range 0.5–24, step 0.5. */
    val overtimeHours: BigDecimal,
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