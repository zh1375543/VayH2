package com.velora.portal.calculation.activitiy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.text.InputType
import androidx.lifecycle.lifecycleScope
import com.velora.portal.R
import com.velora.portal.calculation.SideHomeRepository
import com.velora.portal.calculation.SalaryFormOptions
import com.velora.portal.calculation.model.SetSalaryRequest
import com.velora.portal.core.common.data.bean.SelectionOption
import com.velora.portal.core.common.util.DataNetworkRequest
import com.velora.portal.core.common.util.PermissionCoordinator
import com.velora.portal.core.common.util.viewBinding
import com.velora.portal.core.device.LocationInfoHelper
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.core.ui.component.FormItemView
import com.velora.portal.core.ui.dialog.showConfirmDialog
import com.velora.portal.core.ui.dialog.showOptionPickerDialog
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.databinding.SidepageSetSalaryActivityBinding
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

class SetSalaryActivity : BaseActivity<SidepageSetSalaryActivityBinding>() {

    override val binding by viewBinding(SidepageSetSalaryActivityBinding::inflate)
    private val repository = SideHomeRepository()

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        configureMonthlySalaryInput()

        workingDaysView.setOnClick { showPicker(workingDaysView, SalaryFormOptions.workingDays) }
        hoursPerDayView.setOnClick { showPicker(hoursPerDayView, SalaryFormOptions.workHours) }
        paydayView.setOnClick { showPicker(paydayView, SalaryFormOptions.workingDays) }
        workLocationView.setEndIconClick(::requestLocationPermission)
        tvSave.singleClick {
            if (validateForm()) saveSalaryData()
        }
    }

    private fun configureMonthlySalaryInput() {
        binding.monthlySalaryView.getEditText().apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    private fun showPicker(
        view: FormItemView,
        options: List<SelectionOption>,
    ) {
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
            hours >= SalaryFormOptions.minWorkHours && hours <= SalaryFormOptions.maxWorkHours &&
                hours.remainder(SalaryFormOptions.workHoursStep).compareTo(BigDecimal.ZERO) == 0
        } ?: false
        val paydayValid = paydayView.getText().toIntOrNull() in
            SalaryFormOptions.MIN_WORKING_DAY..SalaryFormOptions.MAX_WORKING_DAY

        if (!salaryValid) monthlySalaryView.showError(getString(R.string.calculator_monthly_salary_error))
        if (!workingDaysValid) workingDaysView.showError(getString(R.string.calculator_working_days_error))
        if (!workHoursValid) hoursPerDayView.showError(getString(R.string.calculator_work_hours_error))
        if (!paydayValid) paydayView.showError(getString(R.string.calculator_payday_error))

        salaryValid && workingDaysValid && workHoursValid && paydayValid
    }

    private fun saveSalaryData() = with(binding) {
        val request = SetSalaryRequest(
            monthlySalary = monthlySalaryView.getText().toBigDecimal(),
            workingDays = workingDaysView.getText().toInt(),
            workHoursPerDay = hoursPerDayView.getText().toBigDecimal(),
            paydayDay = paydayView.getText().toInt(),
            workLocation = workLocationView.getText(),
        )
        DataNetworkRequest(lifecycleScope) {
            repository.saveSalaryData(request)
        }.showLoading()
            .onSuccess { finish() }
            .execute()
    }

    private fun requestLocationPermission() {
        PermissionCoordinator.request(
            activity = this,
            permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            onDenied = { isPermanentlyDenied, permissions ->
                if (isPermanentlyDenied) {
                    showConfirmDialog(
                        title = String.format(
                            getString(R.string.dialog_permission_title),
                            getString(R.string.dialog_permission_location),
                        ),
                        desc = "",
                    ) {
                        PermissionCoordinator.openSystemSettings(this, permissions)
                    }
                }
            },
            showSettingsGuide = false,
        ) {
            loadCurrentLocation()
        }
    }

    private fun loadCurrentLocation() {
        lifecycleScope.launch {
            val (location, address) = LocationInfoHelper.getLocationInfo()
            location ?: return@launch
            val locationText = address?.getAddressLine(0)?.takeIf(String::isNotBlank)
                ?: String.format(
                    Locale.ENGLISH,
                    "%.6f, %.6f",
                    location.latitude,
                    location.longitude,
                )
            binding.workLocationView.setText(locationText)
        }
    }

    companion object {
        private const val MAX_SALARY_DECIMAL_PLACES = 2

        fun launch(context: Context) {
            context.startActivity(Intent(context, SetSalaryActivity::class.java))
        }
    }
}
