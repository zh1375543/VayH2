package com.velora.portal.calculation.fragment

import com.velora.portal.R
import com.velora.portal.calculation.activitiy.BudgetTipDetailActivity
import com.velora.portal.calculation.activitiy.ExpenseTrackingTipDetailActivity
import com.velora.portal.calculation.activitiy.SavingsGoalTipDetailActivity
import com.velora.portal.calculation.activitiy.SavingsTipDetailActivity
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseFragment
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.FragmentCalculationTipsBinding

class CalculationTipsFragment : BaseFragment<FragmentCalculationTipsBinding>(
    R.layout.fragment_calculation_tips,
) {

    override val binding by viewBinding(FragmentCalculationTipsBinding::bind)

    override fun initView() = with(binding) {
        cardSalaryTip.singleClick {
            SavingsTipDetailActivity.launch(requireContext())
        }
        cardBudgetTip.singleClick {
            BudgetTipDetailActivity.launch(requireContext())
        }
        cardExpensesTip.singleClick {
            ExpenseTrackingTipDetailActivity.launch(requireContext())
        }
        cardGoalTip.singleClick {
            SavingsGoalTipDetailActivity.launch(requireContext())
        }
    }

    override fun initObserve() = Unit
}
