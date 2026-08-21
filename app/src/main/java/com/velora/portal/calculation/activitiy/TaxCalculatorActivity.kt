package com.velora.portal.calculation.activitiy

import android.content.Context
import android.content.Intent
import android.text.InputType
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.calculation.SideHomeViewModel
import com.velora.portal.calculation.model.IncomePeriod
import com.velora.portal.calculation.model.TaxCalculationRequest
import com.velora.portal.calculation.model.TaxCalculationResponse
import com.velora.portal.core.common.data.bean.SelectionOption
import com.velora.portal.core.common.util.PageLoadState
import com.velora.portal.core.common.util.viewBinding
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.core.ui.dialog.showOptionPickerDialog
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.databinding.SidepageTaxCalculatorActivityBinding
import java.math.BigDecimal
import java.math.RoundingMode

/** Tax calculator with input and analysis states in one activity. */
class TaxCalculatorActivity : BaseActivity<SidepageTaxCalculatorActivityBinding>() {

    override val binding by viewBinding(SidepageTaxCalculatorActivityBinding::inflate)
    private val viewModel by viewModels<SideHomeViewModel>()
    private var selectedIncomePeriod: IncomePeriod? = null

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        listOf(grossIncomeView, additionalIncomeView, deductionsView).forEach { field ->
            field.getEditText().inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        incomePeriodView.setOnClick(::showIncomePeriodPicker)
        tvCalculate.singleClick {
            if (validateForm()) calculateTax()
        }
    }

    override fun initObserve() = with(viewModel) {
        taxCalculationState.observe(this@TaxCalculatorActivity) { state ->
            when (state) {
                PageLoadState.Loading -> binding.tvCalculate.isEnabled = false
                PageLoadState.Empty,
                PageLoadState.Error -> binding.tvCalculate.isEnabled = true

                is PageLoadState.Content -> {
                    binding.tvCalculate.isEnabled = true
                    renderTaxAnalysis(state.data)
                }
            }
        }
    }

    private fun showIncomePeriodPicker() {
        val options = IncomePeriod.entries.mapIndexed { index, period ->
            SelectionOption(info = period.name, id = index)
        }
        val selectedIndex = selectedIncomePeriod
            ?.let(IncomePeriod.entries::indexOf)
            ?.coerceAtLeast(0)
            ?: 0
        showOptionPickerDialog(selectedIndex, options) { position ->
            val period = IncomePeriod.entries[position]
            selectedIncomePeriod = period
            binding.incomePeriodView.setText(period.name)
            binding.incomePeriodView.hideError()
        }
    }

    private fun calculateTax() = with(binding) {
        val incomePeriod = selectedIncomePeriod ?: return@with
        viewModel.getCalTax(
            TaxCalculationRequest(
                incomePeriod = incomePeriod.name,
                grossIncome = grossIncomeView.getText().toBigDecimal(),
                additionalIncome = additionalIncomeView.getText().toOptionalBigDecimal(),
                deductions = deductionsView.getText().toOptionalBigDecimal(),
            ),
        )
    }

    private fun validateForm(): Boolean = with(binding) {
        val incomePeriodValid = selectedIncomePeriod != null
        val grossIncomeValid = grossIncomeView.getText()
            .toBigDecimalOrNull()
            ?.let { it > BigDecimal.ZERO && it.scale() <= MAX_DECIMAL_PLACES }
            ?: false
        val additionalIncomeValid = additionalIncomeView.getText().isBlank() ||
            additionalIncomeView.getText().isValidOptionalAmount()
        val deductionsValid = deductionsView.getText().isBlank() ||
            deductionsView.getText().isValidOptionalAmount()

        if (!incomePeriodValid) incomePeriodView.showError(getString(R.string.calculator_income_period_error))
        if (!grossIncomeValid) grossIncomeView.showError(getString(R.string.calculator_gross_income_error))
        if (!additionalIncomeValid) {
            additionalIncomeView.showError(getString(R.string.calculator_optional_amount_error))
        }
        if (!deductionsValid) deductionsView.showError(getString(R.string.calculator_optional_amount_error))

        incomePeriodValid && grossIncomeValid && additionalIncomeValid && deductionsValid
    }

    /** Call this after the tax-calculation API succeeds. */
    fun showTaxAnalysis(
        deductions: BigDecimal,
        taxableIncome: BigDecimal,
        estimatedTax: BigDecimal,
        takeHomeIncome: BigDecimal,
    ) = with(binding) {
        taxInputScrollView.isVisible = false
        tvCalculate.isVisible = false
        analysisCard.isVisible = true
        tvDeductionsValue.text = deductions.toCurrencyValue()
        tvTaxableIncomeValue.text = taxableIncome.toCurrencyValue()
        tvEstimatedTaxValue.text = estimatedTax.toCurrencyValue()
        tvTakeHomeIncomeValue.text = takeHomeIncome.toCurrencyValue()
    }

    private fun renderTaxAnalysis(response: TaxCalculationResponse) {
        showTaxAnalysis(
            deductions = response.deductions ?: BigDecimal.ZERO,
            taxableIncome = response.taxableIncome ?: BigDecimal.ZERO,
            estimatedTax = response.estimatedTax ?: BigDecimal.ZERO,
            takeHomeIncome = response.takeHomeIncome ?: BigDecimal.ZERO,
        )
    }

    private fun String.toOptionalBigDecimal(): BigDecimal? =
        takeIf(String::isNotBlank)?.toBigDecimal()

    private fun String.isValidOptionalAmount(): Boolean = toBigDecimalOrNull()?.let {
        it >= BigDecimal.ZERO && it.scale() <= MAX_DECIMAL_PLACES
    } ?: false

    private fun BigDecimal.toCurrencyValue(): String =
        CURRENCY_SYMBOL + setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP).toPlainString()

    companion object {
        private const val MAX_DECIMAL_PLACES = 2
        private const val CURRENCY_DECIMAL_PLACES = 2
        private const val CURRENCY_SYMBOL = "₱"

        fun launch(context: Context) {
            context.startActivity(Intent(context, TaxCalculatorActivity::class.java))
        }
    }
}
