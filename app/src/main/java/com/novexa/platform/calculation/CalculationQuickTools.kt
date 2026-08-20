package com.novexa.platform.calculation

import com.novexa.platform.R
import com.novexa.platform.calculation.activitiy.OvertimeCalculatorActivity
import com.novexa.platform.calculation.activitiy.SalaryCalculatorActivity
import com.novexa.platform.calculation.activitiy.TaxCalculatorActivity
import com.novexa.platform.calculation.activitiy.WorkHoursCalculatorActivity
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.databinding.ItemCalculationQuickToolBinding
import com.novexa.platform.databinding.ViewCalculationQuickToolsBinding

fun ViewCalculationQuickToolsBinding.bindCalculationQuickTools(
    onSalaryClick: (() -> Unit)? = null,
    onOvertimeClick: (() -> Unit)? = null,
    onWorkHoursClick: (() -> Unit)? = null,
    onTaxClick: (() -> Unit)? = null,
) = with(this) {
    bindTool(toolSalary, R.mipmap.page_salary_ic, R.string.calculator_salary_calculator) {
        onSalaryClick?.invoke() ?: SalaryCalculatorActivity.launch(root.context)
    }
    bindTool(toolOvertime, R.mipmap.page_overtime_ic, R.string.calculator_overtime_calculator) {
        onOvertimeClick?.invoke() ?: OvertimeCalculatorActivity.launch(root.context)
    }
    bindTool(toolWorkHours, R.mipmap.page_work_hors_ic, R.string.calculator_work_hours_calculator) {
        onWorkHoursClick?.invoke() ?: WorkHoursCalculatorActivity.launch(root.context)
    }
    bindTool(toolTax, R.mipmap.page_tax_ic, R.string.calculator_tax_calculator) {
        onTaxClick?.invoke() ?: TaxCalculatorActivity.launch(root.context)
    }
}

private fun bindTool(
    item: ItemCalculationQuickToolBinding,
    iconRes: Int,
    titleRes: Int,
    onClick: () -> Unit,
) = with(item) {
    ivToolIcon.setImageResource(iconRes)
    tvToolName.setText(titleRes)
    root.singleClick { onClick() }
}
