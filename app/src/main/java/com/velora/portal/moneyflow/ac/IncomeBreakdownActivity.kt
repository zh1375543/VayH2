package com.velora.portal.moneyflow.ac

import android.content.Context
import android.content.Intent
import android.text.InputType
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.moneyflow.SalaryFormOptions
import com.velora.portal.moneyflow.SideHomeViewModel
import com.velora.portal.moneyflow.model.SalaryCalculationRequest
import com.velora.portal.moneyflow.model.SalaryCalculationResponse
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.data.bean.SelectionOption
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.design.component.FormItemView
import com.velora.portal.platform.design.dialog.showOptionPickerDialog
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.ActivityIncomeBreakdownBinding
import java.math.BigDecimal
import java.math.RoundingMode

/** Salary calculator with input and analysis states in one activity. */
class IncomeBreakdownActivity : BaseActivity<ActivityIncomeBreakdownBinding>() {

    override val binding by viewBinding(ActivityIncomeBreakdownBinding::inflate)
    private val viewModel by viewModels<SideHomeViewModel>()

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        monthlySalaryView.getEditText().inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        workingDaysView.setOnClick { showPicker(workingDaysView, SalaryFormOptions.workingDays) }
        hoursPerDayView.setOnClick { showPicker(hoursPerDayView, SalaryFormOptions.workHours) }
        tvCalculate.singleClick {
            if (validateForm()) {
                viewModel.getCalSalary(
                    SalaryCalculationRequest(
                        useSavedSalary = false,
                        monthlySalary = monthlySalaryView.getText().toBigDecimal(),
                        workingDays = workingDaysView.getText().toInt(),
                        workHoursPerDay = hoursPerDayView.getText().toBigDecimal(),
                    ),
                )
            }
        }
    }

    override fun initObserve() = with(viewModel) {
        salaryCalculationState.observe(this@IncomeBreakdownActivity) { state ->
            when (state) {
                PageLoadState.Loading -> binding.tvCalculate.isEnabled = false
                PageLoadState.Empty,
                PageLoadState.Error -> binding.tvCalculate.isEnabled = true

                is PageLoadState.Content -> {
                    binding.tvCalculate.isEnabled = true
                    renderSalaryAnalysis(state.data)
                }
            }
        }
    }

    private fun showPicker(view: FormItemView, options: List<SelectionOption>) {
        val selectedIndex = options.indexOfFirst { it.info == view.getText() }.coerceAtLeast(0)
        showOptionPickerDialog(selectedIndex, options) { position ->
            view.setText(options[position].info)
            view.hideError()
        }
    }

    private fun validateForm(): Boolean = with(binding) {
        val salaryValid = monthlySalaryView.getText()
            .toBigDecimalOrNull()
            ?.let { it > BigDecimal.ZERO && it.scale() <= MAX_SALARY_DECIMAL_PLACES }
            ?: false
        val workingDaysValid = workingDaysView.getText().toIntOrNull() in
            SalaryFormOptions.MIN_WORKING_DAY..SalaryFormOptions.MAX_WORKING_DAY
        val workHoursValid = hoursPerDayView.getText().toBigDecimalOrNull()?.let { hours ->
            hours >= SalaryFormOptions.minWorkHours &&
                hours <= SalaryFormOptions.maxWorkHours &&
                hours.remainder(SalaryFormOptions.workHoursStep).compareTo(BigDecimal.ZERO) == 0
        } ?: false

        if (!salaryValid) monthlySalaryView.showError(getString(R.string.calculator_monthly_salary_error))
        if (!workingDaysValid) workingDaysView.showError(getString(R.string.calculator_working_days_error))
        if (!workHoursValid) hoursPerDayView.showError(getString(R.string.calculator_work_hours_error))

        salaryValid && workingDaysValid && workHoursValid
    }

    /** Call this after the salary-calculation API succeeds. */
    fun showSalaryAnalysis(
        monthlySalary: BigDecimal,
        dailyWage: BigDecimal,
        hourlyWage: BigDecimal,
    ) = with(binding) {
        salaryInputScrollView.isVisible = false
        tvCalculate.isVisible = false
        analysisCard.isVisible = true
        tvMonthlySalaryValue.text = monthlySalary.toCurrencyValue()
        tvDailyWageValue.text = dailyWage.toCurrencyValue()
        tvHourlyWageValue.text = hourlyWage.toCurrencyValue()
    }

    private fun renderSalaryAnalysis(response: SalaryCalculationResponse) {
        showSalaryAnalysis(
            monthlySalary = response.monthlySalary
                ?: binding.monthlySalaryView.getText().toBigDecimalOrNull()
                ?: BigDecimal.ZERO,
            dailyWage = response.dailyWage ?: BigDecimal.ZERO,
            hourlyWage = response.hourlyWage ?: BigDecimal.ZERO,
        )
    }

    private fun BigDecimal.toCurrencyValue(): String =
        setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP).toPlainString()

    companion object {
        private const val MAX_SALARY_DECIMAL_PLACES = 2
        private const val CURRENCY_DECIMAL_PLACES = 2

        fun launch(context: Context) {
            context.startActivity(Intent(context, IncomeBreakdownActivity::class.java))
        }
    }
}
