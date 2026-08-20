package com.novexa.platform.calculation.fragment

import com.novexa.platform.R
import com.novexa.platform.calculation.activitiy.BudgetTipDetailActivity
import com.novexa.platform.calculation.activitiy.ExpenseTrackingTipDetailActivity
import com.novexa.platform.calculation.activitiy.SavingsGoalTipDetailActivity
import com.novexa.platform.calculation.activitiy.SavingsTipDetailActivity
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.core.ui.base.BaseFragment
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.databinding.FragmentCalculationTipsBinding

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
