package com.velora.portal.moneyflow

import com.velora.portal.R
import com.velora.portal.moneyflow.ac.ExtraPayEstimatorActivity
import com.velora.portal.moneyflow.ac.IncomeBreakdownActivity
import com.velora.portal.moneyflow.ac.WithholdingEstimatorActivity
import com.velora.portal.moneyflow.ac.ShiftDurationActivity
import com.velora.portal.platform.common.util.platform.requireLogin
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.ItemMoneyflowQuickToolBinding
import com.velora.portal.databinding.ViewMoneyflowQuickToolsBinding

fun ViewMoneyflowQuickToolsBinding.bindCalculationQuickTools(
    onSalaryClick: (() -> Unit)? = null,
    onOvertimeClick: (() -> Unit)? = null,
    onWorkHoursClick: (() -> Unit)? = null,
    onTaxClick: (() -> Unit)? = null,
) = with(this) {
    bindTool(toolSalary, R.mipmap.img_salary_cal, R.string.calculator_salary_calculator) {
        root.context.requireLogin {
            onSalaryClick?.invoke() ?: IncomeBreakdownActivity.launch(root.context)
        }
    }
    bindTool(toolOvertime, R.mipmap.img_overtime_cal, R.string.calculator_overtime_calculator) {
        root.context.requireLogin {
            onOvertimeClick?.invoke() ?: ExtraPayEstimatorActivity.launch(root.context)
        }
    }
    bindTool(toolWorkHours, R.mipmap.img_workhour_cal, R.string.calculator_work_hours_calculator) {
        root.context.requireLogin {
            onWorkHoursClick?.invoke() ?: ShiftDurationActivity.launch(root.context)
        }
    }
    bindTool(toolTax, R.mipmap.img_tax_cal, R.string.calculator_tax_calculator) {
        root.context.requireLogin {
            onTaxClick?.invoke() ?: WithholdingEstimatorActivity.launch(root.context)
        }
    }
}

private fun bindTool(
    item: ItemMoneyflowQuickToolBinding,
    iconRes: Int,
    titleRes: Int,
    onClick: () -> Unit,
) = with(item) {
    ivToolIcon.setImageResource(iconRes)
    tvToolName.setText(titleRes)
    root.singleClick { onClick() }
}
