package com.velora.portal.calculation.model

import java.math.BigDecimal

/** Response returned from the tax calculation API. */
data class TaxCalculationResponse(
    val incomePeriod: String? = null,
    val incomePeriodName: String? = null,
    val annualGrossIncome: BigDecimal? = null,
    val deductions: BigDecimal? = null,
    val taxableIncome: BigDecimal? = null,
    /** Tax rate in percentage, e.g. 15.00 means 15%. */
    val taxRate: BigDecimal? = null,
    val estimatedTax: BigDecimal? = null,
    val takeHomeIncome: BigDecimal? = null,
    val currencyCode: String? = null,
    /** Whether the result is estimated. */
    val estimated: Boolean = false,
)
