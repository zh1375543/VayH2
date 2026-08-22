package com.velora.portal.moneyflow

import com.velora.portal.platform.common.data.bean.SelectionOption
import java.math.BigDecimal

/** Shared picker options for salary-related forms. */
object SalaryFormOptions {
    const val MIN_WORKING_DAY = 1
    const val MAX_WORKING_DAY = 30
    val minWorkHours: BigDecimal = BigDecimal("0.5")
    val maxWorkHours: BigDecimal = BigDecimal("24")
    val workHoursStep: BigDecimal = BigDecimal("0.5")

    val workingDays: List<SelectionOption> = (MIN_WORKING_DAY..MAX_WORKING_DAY).map { day ->
        SelectionOption(info = day.toString(), id = day)
    }
    val workHours: List<SelectionOption> = (1..48).map { index ->
        val hours = BigDecimal(index).divide(BigDecimal(2))
        SelectionOption(info = hours.stripTrailingZeros().toPlainString(), id = index)
    }
}
