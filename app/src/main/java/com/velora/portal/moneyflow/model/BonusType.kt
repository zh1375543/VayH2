package com.velora.portal.moneyflow.model

import androidx.annotation.StringRes
import com.velora.portal.R

/** Bonus types accepted by the bonus-calculation API. Submit [name]. */
enum class BonusType(@get:StringRes val displayRes: Int) {
    THIRTEENTH_MONTH(R.string.calculator_bonus_thirteenth_month),
    PERFORMANCE(R.string.calculator_bonus_performance),
    ANNUAL(R.string.calculator_bonus_annual),
    OTHER(R.string.calculator_bonus_other),
}
