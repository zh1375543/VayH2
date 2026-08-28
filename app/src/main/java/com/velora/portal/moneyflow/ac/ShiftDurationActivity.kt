package com.velora.portal.moneyflow.ac

import android.content.Context
import android.content.Intent
import android.text.InputType
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.moneyflow.SalaryFormOptions
import com.velora.portal.moneyflow.SideHomeViewModel
import com.velora.portal.moneyflow.model.SetSalaryResponse
import com.velora.portal.moneyflow.model.WorkHoursCalculationRequest
import com.velora.portal.moneyflow.model.WorkHoursCalculationResponse
import com.velora.portal.platform.common.data.bean.SelectionOption
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.design.component.FormItemView
import com.velora.portal.platform.design.dialog.showOptionPickerDialog
import com.velora.portal.platform.design.dialog.showWorkTimePickerDialog
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.databinding.ActivityShiftDurationBinding
import java.math.BigDecimal
import java.math.RoundingMode

/** Work-hours calculator with input and analysis states in one activity. */
class ShiftDurationActivity : BaseActivity<ActivityShiftDurationBinding>() {

    override val binding by viewBinding(ActivityShiftDurationBinding::inflate)
    private val viewModel by viewModels<SideHomeViewModel>()
    private var startTime: WorkTime? = null
    private var endTime: WorkTime? = null
    private var selectedBreakMinutes: Int? = null

    override fun initView() {
        setupTimeCalculatorLayout()
        bindTimeEntryActions()
        bindCalculationControls()
    }

    private fun setupTimeCalculatorLayout() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        monthlySalaryView.getEditText().inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private fun bindTimeEntryActions() = with(binding) {
        startTimeView.setOnClick { openWorkTimeSelector(isStartTime = true) }
        endTimeView.setOnClick { openWorkTimeSelector(isStartTime = false) }
        breakTimeView.setOnClick(::openBreakDurationSelector)
        workingDaysView.setOnClick { showPicker(workingDaysView, SalaryFormOptions.workingDays) }
        hoursPerDayView.setOnClick { showPicker(hoursPerDayView, SalaryFormOptions.workHours) }
    }

    private fun bindCalculationControls() = with(binding) {
        basedOnSalaryToggle.singleClick {
            ivBasedOnSalary.isSelected = !ivBasedOnSalary.isSelected
            if (ivBasedOnSalary.isSelected) viewModel.getSalaryData()
        }
        tvCalculate.singleClick {
            if (validateForm()) submitWorkHoursCalculation()
        }
    }

    override fun initObserve() = with(viewModel) {
        workHoursCalculationState.observe(this@ShiftDurationActivity) { state ->
            when (state) {
                PageLoadState.Loading -> binding.tvCalculate.isEnabled = false
                PageLoadState.Empty,
                PageLoadState.Error -> binding.tvCalculate.isEnabled = true

                is PageLoadState.Content -> {
                    binding.tvCalculate.isEnabled = true
                    renderWorkHoursAnalysis(state.data)
                }
            }
        }
        salaryDataState.observe(this@ShiftDurationActivity) { state ->
            if (state is PageLoadState.Content) renderSalaryData(state.data)
        }
    }

    private fun openWorkTimeSelector(isStartTime: Boolean) {
        val selected = if (isStartTime) startTime else endTime
        showWorkTimePickerDialog(selected?.hour ?: 0, selected?.minute ?: 0) { hour, minute ->
            val time = WorkTime(hour, minute)
            if (isStartTime) {
                startTime = time
                binding.startTimeView.setText(time.asDisplayText())
                binding.startTimeView.hideError()
            } else {
                endTime = time
                binding.endTimeView.setText(time.asDisplayText())
                binding.endTimeView.hideError()
            }
        }
    }

    private fun openBreakDurationSelector() {
        val selectedIndex = selectedBreakMinutes
            ?.let(BREAK_MINUTES::indexOf)
            ?.coerceAtLeast(0)
            ?: 0
        val options = BREAK_MINUTES.mapIndexed { index, minutes ->
            SelectionOption(info = minutes.toString(), id = index)
        }
        showOptionPickerDialog(selectedIndex, options) { position ->
            val minutes = BREAK_MINUTES[position]
            selectedBreakMinutes = minutes
            binding.breakTimeView.setText(minutes.toString())
            binding.breakTimeView.hideError()
        }
    }

    private fun showPicker(view: FormItemView, options: List<SelectionOption>) {
        val selectedIndex = options.indexOfFirst { it.info == view.getText() }.coerceAtLeast(0)
        showOptionPickerDialog(selectedIndex, options) { position ->
            view.setText(options[position].info)
            view.hideError()
        }
    }

    private fun submitWorkHoursCalculation() = with(binding) {
        val selectedStartTime = startTime ?: return@with
        val selectedEndTime = endTime ?: return@with
        val breakMinutes = selectedBreakMinutes ?: return@with
        val useSavedSalary = ivBasedOnSalary.isSelected
        viewModel.getCalWorkHours(
            WorkHoursCalculationRequest(
                startTime = selectedStartTime.asDisplayText(),
                endTime = selectedEndTime.asDisplayText(),
                breakMinutes = breakMinutes,
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

    private fun validateForm(): Boolean = with(binding) {
        val startTimeValid = startTime != null
        val endTimeValid = endTime != null
        val breakTimeValid = selectedBreakMinutes != null
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

        if (!startTimeValid) startTimeView.showError(getString(R.string.calculator_start_time_error))
        if (!endTimeValid) endTimeView.showError(getString(R.string.calculator_end_time_error))
        if (!breakTimeValid) breakTimeView.showError(getString(R.string.calculator_break_time_error))
        if (!salaryValid) monthlySalaryView.showError(getString(R.string.calculator_monthly_salary_error))
        if (!workingDaysValid) workingDaysView.showError(getString(R.string.calculator_working_days_error))
        if (!workHoursValid) hoursPerDayView.showError(getString(R.string.calculator_work_hours_error))

        startTimeValid && endTimeValid && breakTimeValid && salaryValid && workingDaysValid &&
            workHoursValid
    }

    /** Call this after the work-hours calculation API succeeds. */
    fun showWorkHoursAnalysis(
        totalWorkingTime: String,
        effectiveWorkingTime: String,
        estimatedEarnings: BigDecimal,
        hourlyPay: BigDecimal,
    ) = with(binding) {
        workHoursInputScrollView.isVisible = false
        tvCalculate.isVisible = false
        analysisCard.isVisible = true
        tvTotalWorkingTimeValue.text = totalWorkingTime
        tvEffectiveWorkingTimeValue.text = effectiveWorkingTime
        tvEstimatedEarningsValue.text = estimatedEarnings.toCurrencyValue()
        tvHourlyPayValue.text = hourlyPay.toCurrencyValue()
    }

    private fun renderWorkHoursAnalysis(response: WorkHoursCalculationResponse) {
        showWorkHoursAnalysis(
            totalWorkingTime = response.totalWorkingTimeText.orEmpty(),
            effectiveWorkingTime = response.effectiveWorkingTimeText.orEmpty(),
            estimatedEarnings = response.estimatedEarnings ?: BigDecimal.ZERO,
            hourlyPay = response.hourlyPay ?: BigDecimal.ZERO,
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

    private data class WorkTime(
        val hour: Int,
        val minute: Int,
    ) {
        fun asDisplayText(): String = "%02d:%02d".format(hour, minute)
    }

    companion object {
        private const val MAX_SALARY_DECIMAL_PLACES = 2
        private const val CURRENCY_DECIMAL_PLACES = 2
        private const val CURRENCY_SYMBOL = "₱"
        private val BREAK_MINUTES = listOf(0, 30, 60, 90, 120, 150, 180)

        fun launch(context: Context) {
            context.startActivity(Intent(context, ShiftDurationActivity::class.java))
        }
    }
}
