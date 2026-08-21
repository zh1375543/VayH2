package com.velora.portal.calculation

import com.velora.portal.R
import com.velora.portal.calculation.activitiy.OvertimeCalculatorActivity
import com.velora.portal.calculation.activitiy.SalaryCalculatorActivity
import com.velora.portal.calculation.activitiy.TaxCalculatorActivity
import com.velora.portal.calculation.activitiy.WorkHoursCalculatorActivity
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.ItemCalculationQuickToolBinding
import com.velora.portal.databinding.ViewCalculationQuickToolsBinding

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
