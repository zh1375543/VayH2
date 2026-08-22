package com.velora.portal.moneyflow.fg

import com.velora.portal.R
import com.velora.portal.moneyflow.ac.SpendingPlanGuideActivity
import com.velora.portal.moneyflow.ac.ExpenseHabitsGuideActivity
import com.velora.portal.moneyflow.ac.GoalSettingGuideActivity
import com.velora.portal.moneyflow.ac.SavingsPlaybookActivity
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseFragment
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.FragmentMoneyGuideBinding

class MoneyGuideFragment : BaseFragment<FragmentMoneyGuideBinding>(
    R.layout.fragment_money_guide,
) {

    override val binding by viewBinding(FragmentMoneyGuideBinding::bind)

    override fun initView() = with(binding) {
        cardSalaryTip.singleClick {
            SavingsPlaybookActivity.launch(requireContext())
        }
        cardBudgetTip.singleClick {
            SpendingPlanGuideActivity.launch(requireContext())
        }
        cardExpensesTip.singleClick {
            ExpenseHabitsGuideActivity.launch(requireContext())
        }
        cardGoalTip.singleClick {
            GoalSettingGuideActivity.launch(requireContext())
        }
    }

    override fun initObserve() = Unit
}
