package com.velora.portal.moneyflow.model

import androidx.annotation.StringRes
import com.velora.portal.R
import java.math.BigDecimal

/** Overtime types accepted by the calculator API. Submit [name], not the display label. */
enum class OvertimeType(
    @get:StringRes val displayRes: Int,
    val defaultMultiplier: BigDecimal,
) {
    REGULAR_DAY_OT(R.string.calculator_overtime_regular_day, BigDecimal("1.25")),
    REST_DAY(R.string.calculator_overtime_rest_day, BigDecimal("1.30")),
    REST_DAY_OT(R.string.calculator_overtime_rest_day_ot, BigDecimal("1.69")),
    SPECIAL_HOLIDAY(R.string.calculator_overtime_special_holiday, BigDecimal("1.30")),
    REGULAR_HOLIDAY(R.string.calculator_overtime_regular_holiday, BigDecimal("2.00")),
    REGULAR_HOLIDAY_OT(R.string.calculator_overtime_regular_holiday_ot, BigDecimal("2.60")),
}
