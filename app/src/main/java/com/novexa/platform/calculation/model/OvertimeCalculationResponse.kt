package com.novexa.platform.calculation.model

import java.math.BigDecimal

/** Response returned from the overtime calculation API. */
data class OvertimeCalculationResponse(
    val overtimeType: String? = null,
    val overtimeTypeName: String? = null,
    val overtimeMultiplier: BigDecimal? = null,
    /** Source of the multiplier, e.g. "ENUM" for predefined or "CUSTOM" for user-specified. */
    val multiplierSource: String? = null,
    val overtimeHours: BigDecimal? = null,
    val baseHourlyWage: BigDecimal? = null,
    val overtimePay: BigDecimal? = null,
    val currencyCode: String? = null,
)