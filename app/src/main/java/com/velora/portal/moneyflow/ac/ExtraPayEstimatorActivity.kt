package com.velora.portal.moneyflow.ac

import android.content.Context
import android.content.Intent
import android.text.InputType
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.moneyflow.SalaryFormOptions
import com.velora.portal.moneyflow.SideHomeViewModel
import com.velora.portal.moneyflow.model.OvertimeCalculationRequest
import com.velora.portal.moneyflow.model.OvertimeCalculationResponse
import com.velora.portal.moneyflow.model.OvertimeType
import com.velora.portal.moneyflow.model.SetSalaryResponse
import com.velora.portal.platform.common.data.bean.SelectionOption
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.design.component.FormItemView
import com.velora.portal.platform.design.dialog.showOptionPickerDialog
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.ActivityExtraPayEstimatorBinding
import java.math.BigDecimal
import java.math.RoundingMode

/** Overtime calculator with input and analysis states in one activity. */
class ExtraPayEstimatorActivity : BaseActivity<ActivityExtraPayEstimatorBinding>() {

    override val binding by viewBinding(ActivityExtraPayEstimatorBinding::inflate)
    private val viewModel by viewModels<SideHomeViewModel>()
    private var overtimeHours = MIN_OVERTIME_HOURS
    private var selectedOvertimeType: OvertimeType? = null

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        monthlySalaryView.getEditText().inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        overtimeTypeView.setOnClick(::showOvertimeTypePicker)
        workingDaysView.setOnClick { showPicker(workingDaysView, SalaryFormOptions.workingDays) }
        hoursPerDayView.setOnClick { showPicker(hoursPerDayView, SalaryFormOptions.workHours) }
        ivDecrease.singleClick { updateOvertimeHours(overtimeHours - OVERTIME_HOURS_STEP) }
        ivIncrease.singleClick { updateOvertimeHours(overtimeHours + OVERTIME_HOURS_STEP) }
        basedOnSalaryToggle.singleClick {
            ivBasedOnSalary.isSelected = !ivBasedOnSalary.isSelected
            if (ivBasedOnSalary.isSelected) viewModel.getSalaryData()
        }
        tvCalculate.singleClick {
            if (validateForm()) calculateOvertime()
        }
        updateOvertimeHours(overtimeHours)
    }

    override fun initObserve() = with(viewModel) {
        overtimeCalculationState.observe(this@ExtraPayEstimatorActivity) { state ->
            when (state) {
                PageLoadState.Loading -> binding.tvCalculate.isEnabled = false
                PageLoadState.Empty,
                PageLoadState.Error -> binding.tvCalculate.isEnabled = true

                is PageLoadState.Content -> {
                    binding.tvCalculate.isEnabled = true
                    renderOvertimeAnalysis(state.data)
                }
            }
        }
        salaryDataState.observe(this@ExtraPayEstimatorActivity) { state ->
            if (state is PageLoadState.Content) renderSalaryData(state.data)
        }
    }

    private fun showPicker(view: FormItemView, options: List<SelectionOption>) {
        val selectedIndex = options.indexOfFirst { it.info == view.getText() }.coerceAtLeast(0)
        showOptionPickerDialog(selectedIndex, options) { position ->
            view.setText(options[position].info)
            view.hideError()
        }
    }

    private fun showOvertimeTypePicker() {
        val options = OvertimeType.entries.mapIndexed { index, type ->
            SelectionOption(info = type.defaultMultiplier.toPlainString(), id = index)
        }
        val selectedIndex = selectedOvertimeType
            ?.let(OvertimeType.entries::indexOf)
            ?.coerceAtLeast(0)
            ?: 0
        showOptionPickerDialog(selectedIndex, options) { position ->
            val type = OvertimeType.entries[position]
            selectedOvertimeType = type
            binding.overtimeTypeView.setText(type.defaultMultiplier.toPlainString())
            binding.overtimeTypeView.hideError()
        }
    }

    private fun calculateOvertime() = with(binding) {
        val overtimeType = selectedOvertimeType ?: return@with
        val useSavedSalary = ivBasedOnSalary.isSelected
        viewModel.getCalOvertime(
            OvertimeCalculationRequest(
                overtimeType = overtimeType.name,
                overtimeHours = overtimeHours,
                useSavedSalary = useSavedSalary,
                monthlySalary = if (useSavedSalary) {
                    null
                } else {
                    monthlySalaryView.getText().toBigDecimal()
                },
                workingDays = if (useSavedSalary) null else workingDaysView.getText().toInt(),
                workHoursPerDay = if (useSavedSalary) {
                    null
                } else {
                    hoursPerDayView.getText().toBigDecimal()
                },
            ),
        )
    }

    private fun updateOvertimeHours(value: BigDecimal) = with(binding) {
        overtimeHours = value.coerceIn(MIN_OVERTIME_HOURS, MAX_OVERTIME_HOURS)
        tvOvertimeHours.text = overtimeHours.stripTrailingZeros().toPlainString()
        ivDecrease.isEnabled = overtimeHours > MIN_OVERTIME_HOURS
        ivDecrease.alpha = if (ivDecrease.isEnabled) ENABLED_ALPHA else DISABLED_ALPHA
        ivIncrease.isEnabled = overtimeHours < MAX_OVERTIME_HOURS
        ivIncrease.alpha = if (ivIncrease.isEnabled) ENABLED_ALPHA else DISABLED_ALPHA
    }

    private fun validateForm(): Boolean = with(binding) {
        val overtimeTypeValid = selectedOvertimeType != null
        val useSavedSalary = ivBasedOnSalary.isSelected
        val salaryValid = useSavedSalary ||
            (monthlySalaryView.getText()
                .toBigDecimalOrNull()
                ?.let { it > BigDecimal.ZERO && it.scale() <= MAX_SALARY_DECIMAL_PLACES }
                ?: false)
        val workingDaysValid = useSavedSalary || workingDaysView.getText().toIntOrNull() in
            SalaryFormOptions.MIN_WORKING_DAY..SalaryFormOptions.MAX_WORKING_DAY
        val workHoursValid = useSavedSalary ||
            (hoursPerDayView.getText().toBigDecimalOrNull()?.let { hours ->
                hours >= SalaryFormOptions.minWorkHours &&
                    hours <= SalaryFormOptions.maxWorkHours &&
                    hours.remainder(SalaryFormOptions.workHoursStep)
                        .compareTo(BigDecimal.ZERO) == 0
            } ?: false)

        if (!overtimeTypeValid) overtimeTypeView.showError(getString(R.string.calculator_overtime_type_error))
        if (!salaryValid) monthlySalaryView.showError(getString(R.string.calculator_monthly_salary_error))
        if (!workingDaysValid) workingDaysView.showError(getString(R.string.calculator_working_days_error))
        if (!workHoursValid) hoursPerDayView.showError(getString(R.string.calculator_work_hours_error))

        overtimeTypeValid && salaryValid && workingDaysValid && workHoursValid
    }

    /** Call this after the overtime-calculation API succeeds. */
    fun showOvertimeAnalysis(
        overtimeType: String,
        overtimeHours: BigDecimal,
        overtimePay: BigDecimal,
    ) = with(binding) {
        overtimeInputScrollView.isVisible = false
        tvCalculate.isVisible = false
        analysisCard.isVisible = true
        tvAnalysisOvertimeTypeValue.text = overtimeType
        tvAnalysisOvertimeHoursValue.text = overtimeHours.toPlainString()
        tvAnalysisOvertimePayValue.text = overtimePay.toCurrencyValue()
    }

    private fun renderOvertimeAnalysis(response: OvertimeCalculationResponse) {
        val selectedType = selectedOvertimeType
        val overtimeTypeName = response.overtimeTypeName
            ?: selectedType?.let { getString(it.displayRes).substringBefore("(") }
            ?: response.overtimeType.orEmpty()
        val overtimeMultiplier = response.overtimeMultiplier ?: selectedType?.defaultMultiplier
        val overtimeTypeText = if (overtimeMultiplier != null) {
            "$overtimeTypeName(${overtimeMultiplier.toPlainString()})"
        } else {
            overtimeTypeName
        }
        showOvertimeAnalysis(
            overtimeType = overtimeTypeText,
            overtimeHours = response.overtimeHours ?: overtimeHours,
            overtimePay = response.overtimePay ?: BigDecimal.ZERO,
        )
    }

    private fun renderSalaryData(response: SetSalaryResponse) = with(binding) {
        response.monthlySalary?.let { salary ->
            monthlySalaryView.setText(salary.stripTrailingZeros().toPlainString())
            monthlySalaryView.hideError()
        }
        response.workingDays.takeIf { it > 0 }?.let { workingDays ->
            workingDaysView.setText(workingDays.toString())
            workingDaysView.hideError()
        }
        response.workHoursPerDay?.let { workHours ->
            hoursPerDayView.setText(workHours.stripTrailingZeros().toPlainString())
            hoursPerDayView.hideError()
        }
    }

    private fun BigDecimal.toCurrencyValue(): String =
        CURRENCY_SYMBOL + setScale(CURRENCY_DECIMAL_PLACES, RoundingMode.HALF_UP).toPlainString()

    companion object {
        private const val MAX_SALARY_DECIMAL_PLACES = 2
        private const val CURRENCY_DECIMAL_PLACES = 2
        private const val CURRENCY_SYMBOL = "₱"
        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.4f
        private val MIN_OVERTIME_HOURS = BigDecimal("0.5")
        private val MAX_OVERTIME_HOURS = BigDecimal("24")
        private val OVERTIME_HOURS_STEP = BigDecimal("0.5")

        fun launch(context: Context) {
            context.startActivity(Intent(context, ExtraPayEstimatorActivity::class.java))
        }
    }
}
