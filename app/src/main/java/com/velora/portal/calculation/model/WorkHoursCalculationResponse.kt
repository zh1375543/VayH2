package com.velora.portal.calculation.model

import java.math.BigDecimal

/** Response returned from the work hours calculation API. */
data class WorkHoursCalculationResponse(
    /** Whether the work period crosses midnight. */
    val crossDay: Boolean = false,
    /** Total minutes including break. */
    val totalMinutes: Int = 0,
    /** Effective working minutes excluding break. */
    val effectiveMinutes: Int = 0,
    /** Human-readable total working time, e.g. "8 Hours 0 Minutes". */
    val totalWorkingTimeText: String? = null,
    /** Human-readable effective working time, e.g. "7 Hours 0 Minutes". */
    val effectiveWorkingTimeText: String? = null,
    val hourlyPay: BigDecimal? = null,
    val estimatedEarnings: BigDecimal? = null,
    val currencyCode: String? = null,
)
