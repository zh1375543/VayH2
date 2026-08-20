package com.novexa.platform.calculation.fragment

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.novexa.platform.R
import com.novexa.platform.calculation.SideHomeViewModel
import com.novexa.platform.calculation.bindCalculationQuickTools
import com.novexa.platform.calculation.activitiy.SetSalaryActivity
import com.novexa.platform.calculation.model.CalculationHomeResponse
import com.novexa.platform.core.common.util.PageLoadState
import com.novexa.platform.core.common.util.platform.requireLogin
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.core.ui.base.BaseFragment
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.databinding.FragmentCalculationHomeBinding
import com.novexa.platform.databinding.ItemCalculationStatCardBinding
import com.novexa.platform.feature.inbox.presentation.InboxActivity
import java.math.BigDecimal

/** Dashboard shown before and after a member signs in to the calculation area. */
class CalculationHomeFragment : BaseFragment<FragmentCalculationHomeBinding>(
    R.layout.fragment_calculation_home,
) {
    override val binding by viewBinding(FragmentCalculationHomeBinding::bind)
    private val vm by viewModels<SideHomeViewModel>()
    private var profileConfigured: Boolean? = null

    override fun initView() = with(binding) {
        ivMessage.singleClick {
            requireContext().requireLogin {
                requireContext().start<InboxActivity>()
            }
        }
        tvHeroAction.singleClick {
            requireContext().requireLogin {
                SetSalaryActivity.launch(requireContext())
            }
        }
        pageState.setOnRetryClickListener {
            refreshCalculationHome()
        }
        swipeRefreshLayout.setOnRefreshListener {
            refreshCalculationHome()
        }
        renderSessionState()
    }

    override fun onResume() {
        super.onResume()
        if (!SessionStore.isLoggedIn) {
            profileConfigured = null
        }
        refreshCalculationHome()
    }

    private fun refreshCalculationHome() = with(binding) {
        contentLayout.isVisible = false
        pageState.showLoading()
        swipeRefreshLayout.isRefreshing = false
        vm.getCalculationHomeData()
    }

    private fun renderSessionState(
        showMemberContent: Boolean = SessionStore.isLoggedIn && profileConfigured == true,
    ) = with(binding) {
        tvGreetingTitle.setText(R.string.calculator_greeting_title)
        tvGreetingDescription.setText(
            if (showMemberContent) R.string.calculator_greeting_member
            else R.string.calculator_greeting_guest,
        )
        ivEarningsBackground.setImageResource(
            if (showMemberContent) R.mipmap.page_earnings_bg else R.mipmap.ic_main_card_bg,
        )
        layoutGuestEarnings.isVisible = !showMemberContent
        layoutMemberEarnings.isVisible = showMemberContent
        tvMemberEarningsValue.setText(R.string.calculator_empty_currency)

        bindStatCard(
            cardMonthlySalary,
            R.mipmap.page_monthly_ic,
            getString(R.string.calculator_monthly_salary),
            getString(R.string.calculator_empty_currency),
        )
        bindStatCard(
            cardNextPayday,
            R.mipmap.page_payday_ic,
            getString(R.string.calculator_next_payday),
            getString(R.string.calculator_none),
        )
        bindStatCard(
            cardDailySalary,
            R.mipmap.page_daily_ic,
            getString(R.string.calculator_daily_salary),
            getString(R.string.calculator_empty_currency),
        )
        bindStatCard(
            cardMonthlyGoal,
            R.mipmap.page_goal_ic,
            getString(R.string.calculator_monthly_goal),
            getString(R.string.calculator_none),
        )

        quickTools.bindCalculationQuickTools()
    }

    private fun renderHomeData(homeData: CalculationHomeResponse) = with(binding) {
        profileConfigured = homeData.profileConfigured
        val showMemberContent = SessionStore.isLoggedIn && profileConfigured == true
        renderSessionState(showMemberContent)
        if (!showMemberContent) return@with

        homeData.greeting?.takeIf(String::isNotBlank)?.let { tvGreetingTitle.text = it }
        homeData.incomeComparison?.message?.takeIf(String::isNotBlank)?.let {
            tvGreetingDescription.text = it
        }
        homeData.todayEarnings?.let { tvMemberEarningsValue.text = it.formatAmountWithPrefix() }
        homeData.monthlySalary?.let {
            bindStatCard(
                cardMonthlySalary,
                R.mipmap.page_monthly_ic,
                getString(R.string.calculator_monthly_salary),
                it.formatAmountWithPrefix(),
            )
        }
        homeData.dailySalary?.let {
            bindStatCard(
                cardDailySalary,
                R.mipmap.page_daily_ic,
                getString(R.string.calculator_daily_salary),
                it.formatAmountWithPrefix(),
            )
        }
        homeData.nextPayday?.let { payday ->
            bindStatCard(
                cardNextPayday,
                R.mipmap.page_payday_ic,
                getString(R.string.calculator_next_payday),
                getString(R.string.calculator_days_left, payday.daysLeft),
                payday.date,
            )
        }
        homeData.monthlyGoal?.takeIf { it.configured }?.let { goal ->
            bindStatCard(
                cardMonthlyGoal,
                R.mipmap.page_goal_ic,
                getString(R.string.calculator_monthly_goal),
                goal.progress.formatPercentage(),
                goal.currentSavings?.let { current ->
                    goal.savingsGoal?.let { target ->
                        "${current.formatAmountWithPrefix()}/${target.formatAmountWithPrefix()}"
                    }
                },
            )
        }
    }

    private fun bindStatCard(
        card: ItemCalculationStatCardBinding,
        iconRes: Int,
        title: CharSequence,
        value: CharSequence,
        description: CharSequence? = null,
    ) = with(card) {
        ivStatIcon.setImageResource(iconRes)
        tvStatTitle.text = title
        tvStatValue.text = value
        tvStatDescription.isVisible = description != null
        tvStatDescription.text = description
    }

    private fun BigDecimal?.formatPercentage(): String {
        val value = this ?: return getString(R.string.calculator_none)
        val percentage = if (value <= BigDecimal.ONE) value.movePointRight(2) else value
        return "${percentage.stripTrailingZeros().toPlainString()}%"
    }

    override fun initObserve() = with(vm) {
        calculationHomeState.observe(this@CalculationHomeFragment) { state ->
            when (state) {
                PageLoadState.Loading -> {
                    binding.contentLayout.isVisible = false
                    binding.pageState.showLoading()
                }

                PageLoadState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.contentLayout.isVisible = false
                    binding.pageState.showError()
                }

                PageLoadState.Empty -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.contentLayout.isVisible = true
                    binding.pageState.hide()
                    renderSessionState()
                }

                is PageLoadState.Content -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.contentLayout.isVisible = true
                    binding.pageState.hide()
                    renderHomeData(state.data)
                }
            }
        }
    }
}
