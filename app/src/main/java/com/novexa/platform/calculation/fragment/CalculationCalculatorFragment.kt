package com.novexa.platform.calculation.fragment

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.novexa.platform.R
import com.novexa.platform.calculation.activitiy.BonusCalculatorActivity
import com.novexa.platform.calculation.activitiy.OvertimeCalculatorActivity
import com.novexa.platform.calculation.activitiy.SalaryCalculatorActivity
import com.novexa.platform.calculation.activitiy.SavingsCalculatorActivity
import com.novexa.platform.calculation.activitiy.TaxCalculatorActivity
import com.novexa.platform.calculation.activitiy.WorkHoursCalculatorActivity
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.core.ui.base.BaseFragment
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.databinding.FragmentCalculationCalculatorBinding
import com.novexa.platform.databinding.ItemCalculatorMenuBinding

/** Entry point for the available calculation tools. */
class CalculationCalculatorFragment : BaseFragment<FragmentCalculationCalculatorBinding>(
    R.layout.fragment_calculation_calculator,
) {

    override val binding by viewBinding(FragmentCalculationCalculatorBinding::bind)

    override fun initView() = with(binding) {
        bindMenu(
            menuSalary,
            R.mipmap.page_cal_salary,
            R.string.calculator_salary_title,
            R.string.calculator_salary_description,
        )
        menuSalary.root.singleClick {
            SalaryCalculatorActivity.launch(requireContext())
        }
        bindMenu(
            menuOvertime,
            R.mipmap.page_cal_overtime,
            R.string.calculator_overtime_title,
            R.string.calculator_overtime_description,
        )
        menuOvertime.root.singleClick {
            OvertimeCalculatorActivity.launch(requireContext())
        }
        bindMenu(
            menuWorkHour,
            R.mipmap.page_cal_work_hour,
            R.string.calculator_work_hour_title,
            R.string.calculator_work_hour_description,
        )
        menuWorkHour.root.singleClick {
            WorkHoursCalculatorActivity.launch(requireContext())
        }
        bindMenu(
            menuTax,
            R.mipmap.page_cal_tax,
            R.string.calculator_tax_title,
            R.string.calculator_tax_description,
        )
        menuTax.root.singleClick {
            TaxCalculatorActivity.launch(requireContext())
        }
        bindMenu(
            menuBonus,
            R.mipmap.page_cal_bonus,
            R.string.calculator_bonus_title,
            R.string.calculator_bonus_description,
        )
        menuBonus.root.singleClick {
            BonusCalculatorActivity.launch(requireContext())
        }
        bindMenu(
            menuSavings,
            R.mipmap.page_cal_savings,
            R.string.calculator_savings_title,
            R.string.calculator_savings_description,
        )
        menuSavings.root.singleClick {
            SavingsCalculatorActivity.launch(requireContext())
        }
    }

    override fun initObserve() = Unit

    private fun bindMenu(
        item: ItemCalculatorMenuBinding,
        @DrawableRes iconRes: Int,
        @StringRes titleRes: Int,
        @StringRes descriptionRes: Int,
    ) = with(item) {
        ivCalculatorIcon.setImageResource(iconRes)
        tvCalculatorTitle.setText(titleRes)
        tvCalculatorDescription.setText(descriptionRes)
    }
}
