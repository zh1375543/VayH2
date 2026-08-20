package com.novexa.platform.calculation.activitiy

import android.content.Context
import android.content.Intent
import android.text.InputType
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.novexa.platform.R
import com.novexa.platform.calculation.SideHomeViewModel
import com.novexa.platform.calculation.model.SavingsCalculationRequest
import com.novexa.platform.calculation.model.SavingsCalculationResponse
import com.novexa.platform.core.common.util.PageLoadState
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.databinding.SidepageSavingsCalculatorActivityBinding
import java.math.BigDecimal
import java.math.RoundingMode

/** Savings calculator with input and analysis states in one activity. */
class SavingsCalculatorActivity : BaseActivity<SidepageSavingsCalculatorActivityBinding>() {

    override val binding by viewBinding(SidepageSavingsCalculatorActivityBinding::inflate)
    private val viewModel by viewModels<SideHomeViewModel>()

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        listOf(savingsGoalView, currentSavingsView, monthlySavingsView, monthlyIncomeView).forEach {
            it.getEditText().inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        tvCalculate.singleClick {
            if (validateForm()) calculateSavings()
        }
    }

    override fun initObserve() = with(viewModel) {
        savingsCalculationState.observe(this@SavingsCalculatorActivity) { state ->
            when (state) {
                PageLoadState.Loading -> binding.tvCalculate.isEnabled = false
                PageLoadState.Empty,
                PageLoadState.Error -> binding.tvCalculate.isEnabled = true

                is PageLoadState.Content -> {
                    binding.tvCalculate.isEnabled = true
                    renderSavingsAnalysis(state.data)
                }
            }
        }
    }

    private fun calculateSavings() = with(binding) {
        viewModel.getCalSavings(
            SavingsCalculationRequest(
                savingsGoal = savingsGoalView.getText().toBigDecimal(),
                currentSavings = currentSavingsView.getText().toBigDecimal(),
                monthlySavings = monthlySavingsView.getText().toBigDecimal(),
                monthlyIncome = monthlyIncomeView.getText().toBigDecimal(),
            ),
        )
    }

    private fun validateForm(): Boolean = with(binding) {
        val savingsGoalValid = savingsGoalView.getText().isValidPositiveAmount()
        val currentSavingsValid = currentSavingsView.getText().isValidNonNegativeAmount()
        val monthlySavingsValid = monthlySavingsView.getText().isValidNonNegativeAmount()
        val monthlyIncomeValid = monthlyIncomeView.getText().isValidNonNegativeAmount()

        if (!savingsGoalValid) savingsGoalView.showError(getString(R.string.calculator_positive_amount_error))
        if (!currentSavingsValid) {
            currentSavingsView.showError(getString(R.string.calculator_non_negative_amount_error))
        }
        if (!monthlySavingsValid) {
            monthlySavingsView.showError(getString(R.string.calculator_non_negative_amount_error))
        }
        if (!monthlyIncomeValid) {
            monthlyIncomeView.showError(getString(R.string.calculator_non_negative_amount_error))
        }

        savingsGoalValid && currentSavingsValid && monthlySavingsValid && monthlyIncomeValid
    }

    /** Call this after the savings-calculation API succeeds. */
    fun showSavingsAnalysis(
        goalAmount: BigDecimal,
        currentSavings: BigDecimal,
        remainingAmount: BigDecimal,
        estimatedCompletionTime: String,
        monthlySavingsRate: BigDecimal,
    ) = with(binding) {
        savingsInputScrollView.isVisible = false
        tvCalculate.isVisible = false
        analysisCard.isVisible = true
        tvGoalAmountValue.text = goalAmount.toCurrencyValue()
        tvCurrentSavingsValue.text = currentSavings.toCurrencyValue()
        tvRemainingAmountValue.text = remainingAmount.toCurrencyValue()
        tvEstimatedCompletionTimeValue.text = estimatedCompletionTime
        tvMonthlySavingsRateValue.text = monthlySavingsRate.toPercentageValue()
    }

    private fun renderSavingsAnalysis(response: SavingsCalculationResponse) = with(binding) {
        val estimatedCompletionTime = response.estimatedCompletionDate
            ?: getString(
                R.string.calculator_completion_months,
                response.estimatedCompletionMonths,
            )
        showSavingsAnalysis(
            goalAmount = response.savingsGoal
                ?: savingsGoalView.getText().toBigDecimalOrNull()
                ?: BigDecimal.ZERO,
            currentSavings = response.currentSavings
                ?: currentSavingsView.getText().toBigDecimalOrNull()
                ?: BigDecimal.ZERO,
            remainingAmount = response.remainingAmount ?: BigDecimal.ZERO,
            estimatedCompletionTime = estimatedCompletionTime,
            monthlySavingsRate = response.monthlySavingsRate ?: BigDecimal.ZERO,
        )
    }

    private fun String.isValidPositiveAmount(): Boolean = toBigDecimalOrNull()?.let {
        it > BigDecimal.ZERO && it.scale() <= MAX_DECIMAL_PLACES
    } ?: false

    private fun String.isValidNonNegativeAmount(): Boolean = toBigDecimalOrNull()?.let {
        it >= BigDecimal.ZERO && it.scale() <= MAX_DECIMAL_PLACES
    } ?: false

    private fun BigDecimal.toCurrencyValue(): String =
        CURRENCY_SYMBOL + setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP).toPlainString()

    private fun BigDecimal.toPercentageValue(): String =
        setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP).toPlainString() + "%"

    companion object {
        private const val MAX_DECIMAL_PLACES = 2
        private const val CURRENCY_DECIMAL_PLACES = 2
        private const val CURRENCY_SYMBOL = "₱"

        fun launch(context: Context) {
            context.startActivity(Intent(context, SavingsCalculatorActivity::class.java))
        }
    }
}
