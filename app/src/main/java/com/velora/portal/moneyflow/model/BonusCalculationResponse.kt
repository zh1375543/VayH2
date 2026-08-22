package com.velora.portal.moneyflow.model

import java.math.BigDecimal

/** Response returned from the bonus calculation API. */
data class BonusCalculationResponse(
    val monthlyBasicSalary: BigDecimal? = null,
    val bonusAmount: BigDecimal? = null,
    val bonusType: String? = null,
    val bonusTypeName: String? = null,
    val taxYear: Int = 0,
    val grossBonus: BigDecimal? = null,
    val taxExemptAmount: BigDecimal? = null,
    val taxableBonus: BigDecimal? = null,
    /** Tax rate in percentage, e.g. 15.00 means 15%. */
    val taxRate: BigDecimal? = null,
    val estimatedTax: BigDecimal? = null,
    val netBonus: BigDecimal? = null,
    val currencyCode: String? = null,
    /** Whether the result is estimated. */
    val estimated: Boolean = false,
)
