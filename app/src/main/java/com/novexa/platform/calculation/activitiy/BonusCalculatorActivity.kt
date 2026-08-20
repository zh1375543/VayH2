package com.novexa.platform.calculation.activitiy

import android.content.Context
import android.content.Intent
import android.text.InputType
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.novexa.platform.R
import com.novexa.platform.calculation.SideHomeViewModel
import com.novexa.platform.calculation.model.BonusCalculationRequest
import com.novexa.platform.calculation.model.BonusCalculationResponse
import com.novexa.platform.calculation.model.BonusType
import com.novexa.platform.core.common.data.bean.SelectionOption
import com.novexa.platform.core.common.util.PageLoadState
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.core.ui.dialog.showOptionPickerDialog
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.databinding.SidepageBonusCalculatorActivityBinding
import java.math.BigDecimal
import java.math.RoundingMode

/** Bonus calculator with input and analysis states in one activity. */
class BonusCalculatorActivity : BaseActivity<SidepageBonusCalculatorActivityBinding>() {

    override val binding by viewBinding(SidepageBonusCalculatorActivityBinding::inflate)
    private val viewModel by viewModels<SideHomeViewModel>()
    private var selectedBonusType: BonusType? = null
    private var selectedTaxYear: Int? = null

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        listOf(monthlyBasicSalaryView, bonusAmountView).forEach { field ->
            field.getEditText().inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        bonusTypeView.setOnClick(::showBonusTypePicker)
        taxYearView.setOnClick(::showTaxYearPicker)
        tvCalculate.singleClick {
            if (validateForm()) calculateBonus()
        }
    }

    override fun initObserve() = with(viewModel) {
        bonusCalculationState.observe(this@BonusCalculatorActivity) { state ->
            when (state) {
                PageLoadState.Loading -> binding.tvCalculate.isEnabled = false
                PageLoadState.Empty,
                PageLoadState.Error -> binding.tvCalculate.isEnabled = true

                is PageLoadState.Content -> {
                    binding.tvCalculate.isEnabled = true
                    renderBonusAnalysis(state.data)
                }
            }
        }
    }

    private fun showBonusTypePicker() {
        val options = BonusType.entries.mapIndexed { index, type ->
            SelectionOption(info = getString(type.displayRes), id = index)
        }
        val selectedIndex = selectedBonusType
            ?.let(BonusType.entries::indexOf)
            ?.coerceAtLeast(0)
            ?: 0
        showOptionPickerDialog(selectedIndex, options) { position ->
            val type = BonusType.entries[position]
            selectedBonusType = type
            binding.bonusTypeView.setText(getString(type.displayRes))
            binding.bonusTypeView.hideError()
        }
    }

    private fun showTaxYearPicker() {
        val options = (MIN_TAX_YEAR..MAX_TAX_YEAR).mapIndexed { index, year ->
            SelectionOption(info = year.toString(), id = index)
        }
        val selectedIndex = (selectedTaxYear ?: DEFAULT_TAX_YEAR) - MIN_TAX_YEAR
        showOptionPickerDialog(selectedIndex, options) { position ->
            selectedTaxYear = MIN_TAX_YEAR + position
            binding.taxYearView.setText(selectedTaxYear.toString())
            binding.taxYearView.hideError()
        }
    }

    private fun calculateBonus() = with(binding) {
        val bonusType = selectedBonusType ?: return@with
        val taxYear = selectedTaxYear ?: return@with
        viewModel.getCalBonus(
            BonusCalculationRequest(
                monthlyBasicSalary = monthlyBasicSalaryView.getText().toBigDecimal(),
                bonusAmount = bonusAmountView.getText().toBigDecimal(),
                bonusType = bonusType.name,
                taxYear = taxYear,
            ),
        )
    }

    private fun validateForm(): Boolean = with(binding) {
        val salaryValid = monthlyBasicSalaryView.getText().isValidPositiveAmount()
        val bonusAmountValid = bonusAmountView.getText().isValidNonNegativeAmount()
        val bonusTypeValid = selectedBonusType != null
        val taxYearValid = selectedTaxYear?.let { it in MIN_TAX_YEAR..MAX_TAX_YEAR } ?: false

        if (!salaryValid) {
            monthlyBasicSalaryView.showError(
                getString(R.string.calculator_monthly_basic_salary_error),
            )
        }
        if (!bonusAmountValid) {
            bonusAmountView.showError(getString(R.string.calculator_bonus_amount_error))
        }
        if (!bonusTypeValid) bonusTypeView.showError(getString(R.string.calculator_bonus_type_error))
        if (!taxYearValid) taxYearView.showError(getString(R.string.calculator_tax_year_error))

        salaryValid && bonusAmountValid && bonusTypeValid && taxYearValid
    }

    /** Call this after the bonus-calculation API succeeds. */
    fun showBonusAnalysis(
        bonusType: String,
        grossBonus: BigDecimal,
        taxExemptAmount: BigDecimal,
        taxableBonus: BigDecimal,
        estimatedTax: BigDecimal,
        netBonus: BigDecimal,
    ) = with(binding) {
        bonusInputScrollView.isVisible = false
        tvCalculate.isVisible = false
        analysisCard.isVisible = true
        tvBonusTypeValue.text = bonusType
        tvGrossBonusValue.text = grossBonus.toCurrencyValue()
        tvTaxExemptAmountValue.text = taxExemptAmount.toCurrencyValue()
        tvTaxableBonusValue.text = taxableBonus.toCurrencyValue()
        tvEstimatedTaxValue.text = estimatedTax.toCurrencyValue()
        tvNetBonusValue.text = netBonus.toCurrencyValue()
    }

    private fun renderBonusAnalysis(response: BonusCalculationResponse) {
        val bonusType = response.bonusTypeName
            ?: selectedBonusType?.let { getString(it.displayRes) }
            ?: response.bonusType.orEmpty()
        showBonusAnalysis(
            bonusType = bonusType,
            grossBonus = response.grossBonus ?: BigDecimal.ZERO,
            taxExemptAmount = response.taxExemptAmount ?: BigDecimal.ZERO,
            taxableBonus = response.taxableBonus ?: BigDecimal.ZERO,
            estimatedTax = response.estimatedTax ?: BigDecimal.ZERO,
            netBonus = response.netBonus ?: BigDecimal.ZERO,
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

    companion object {
        private const val MIN_TAX_YEAR = 2000
        private const val MAX_TAX_YEAR = 2026
        private const val DEFAULT_TAX_YEAR = 2026
        private const val MAX_DECIMAL_PLACES = 2
        private const val CURRENCY_DECIMAL_PLACES = 2
        private const val CURRENCY_SYMBOL = "₱"

        fun launch(context: Context) {
            context.startActivity(Intent(context, BonusCalculatorActivity::class.java))
        }
    }
}
