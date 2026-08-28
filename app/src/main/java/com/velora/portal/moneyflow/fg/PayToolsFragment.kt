package com.velora.portal.moneyflow.fg

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.velora.portal.R
import com.velora.portal.moneyflow.ac.RewardPayoutActivity
import com.velora.portal.moneyflow.ac.ExtraPayEstimatorActivity
import com.velora.portal.moneyflow.ac.IncomeBreakdownActivity
import com.velora.portal.moneyflow.ac.GoalPlannerActivity
import com.velora.portal.moneyflow.ac.WithholdingEstimatorActivity
import com.velora.portal.moneyflow.ac.ShiftDurationActivity
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseFragment
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.FragmentPayToolsBinding
import com.velora.portal.databinding.ItemCalculatorMenuBinding

/** Entry point for the available calculation tools. */
class PayToolsFragment : BaseFragment<FragmentPayToolsBinding>(
    R.layout.fragment_pay_tools,
) {

    override val binding by viewBinding(FragmentPayToolsBinding::bind)

    override fun initView() {
        bindIncomeToolEntries()
        bindWorkToolEntries()
        bindPlanningToolEntries()
    }

    private fun bindIncomeToolEntries() = with(binding) {
        bindMenu(
            menuSalary,
            R.mipmap.image_salary,
            R.string.calculator_salary_title,
            R.string.calculator_salary_description,
        )
        menuSalary.root.singleClick {
            IncomeBreakdownActivity.launch(requireContext())
        }
        bindMenu(
            menuOvertime,
            R.mipmap.image_overtime,
            R.string.calculator_overtime_title,
            R.string.calculator_overtime_description,
        )
        menuOvertime.root.singleClick {
            ExtraPayEstimatorActivity.launch(requireContext())
        }
    }

    private fun bindWorkToolEntries() = with(binding) {
        bindMenu(
            menuWorkHour,
            R.mipmap.image_work,
            R.string.calculator_work_hour_title,
            R.string.calculator_work_hour_description,
        )
        menuWorkHour.root.singleClick {
            ShiftDurationActivity.launch(requireContext())
        }
        bindMenu(
            menuTax,
            R.mipmap.image_tax,
            R.string.calculator_tax_title,
            R.string.calculator_tax_description,
        )
        menuTax.root.singleClick {
            WithholdingEstimatorActivity.launch(requireContext())
        }
    }

    private fun bindPlanningToolEntries() = with(binding) {
        bindMenu(
            menuBonus,
            R.mipmap.image_bonus,
            R.string.calculator_bonus_title,
            R.string.calculator_bonus_description,
        )
        menuBonus.root.singleClick {
            RewardPayoutActivity.launch(requireContext())
        }
        bindMenu(
            menuSavings,
            R.mipmap.image_saving,
            R.string.calculator_savings_title,
            R.string.calculator_savings_description,
        )
        menuSavings.root.singleClick {
            GoalPlannerActivity.launch(requireContext())
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
