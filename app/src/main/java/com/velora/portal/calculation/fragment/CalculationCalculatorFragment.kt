package com.velora.portal.calculation.fragment

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.velora.portal.R
import com.velora.portal.calculation.activitiy.BonusCalculatorActivity
import com.velora.portal.calculation.activitiy.OvertimeCalculatorActivity
import com.velora.portal.calculation.activitiy.SalaryCalculatorActivity
import com.velora.portal.calculation.activitiy.SavingsCalculatorActivity
import com.velora.portal.calculation.activitiy.TaxCalculatorActivity
import com.velora.portal.calculation.activitiy.WorkHoursCalculatorActivity
import com.velora.portal.core.common.util.viewBinding
import com.velora.portal.core.ui.base.BaseFragment
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.databinding.FragmentCalculationCalculatorBinding
import com.velora.portal.databinding.ItemCalculatorMenuBinding

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
