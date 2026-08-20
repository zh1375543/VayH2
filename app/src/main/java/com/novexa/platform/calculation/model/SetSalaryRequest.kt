package com.novexa.platform.calculation.model

import com.novexa.platform.BuildConfig
import com.novexa.platform.core.common.data.APPCODE
import java.math.BigDecimal

/** Request parameters used to set the user's salary configuration. */
data class SetSalaryRequest(
    val monthlySalary: BigDecimal,
    val workingDays: Int,
    val workHoursPerDay: BigDecimal,
    val paydayDay: Int,
    val workLocation: String,
    val version: String? = BuildConfig.VERSION_NAME,
    val mobileType: String = "2",
    val appCode: String = APPCODE,
)