package com.velora.portal.calculation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.velora.portal.calculation.model.BonusCalculationRequest
import com.velora.portal.calculation.model.BonusCalculationResponse
import com.velora.portal.calculation.model.CalculationHomeRequest
import com.velora.portal.calculation.model.CalculationHomeResponse
import com.velora.portal.calculation.model.OvertimeCalculationRequest
import com.velora.portal.calculation.model.OvertimeCalculationResponse
import com.velora.portal.calculation.model.SalaryCalculationRequest
import com.velora.portal.calculation.model.SalaryCalculationResponse
import com.velora.portal.calculation.model.SavingsCalculationRequest
import com.velora.portal.calculation.model.SavingsCalculationResponse
import com.velora.portal.calculation.model.SetSalaryResponse
import com.velora.portal.calculation.model.TaxCalculationRequest
import com.velora.portal.calculation.model.TaxCalculationResponse
import com.velora.portal.calculation.model.WorkHoursCalculationRequest
import com.velora.portal.calculation.model.WorkHoursCalculationResponse
import com.velora.portal.core.common.util.PageLoadState
import com.velora.portal.core.ui.base.BaseViewModel

class SideHomeViewModel(
    private val repository: SideHomeRepository = SideHomeRepository(),
) : BaseViewModel() {

    private val _calculationHomeState = MutableLiveData<PageLoadState<CalculationHomeResponse>>(
        PageLoadState.Loading,
    )
    val calculationHomeState: LiveData<PageLoadState<CalculationHomeResponse>> =
        _calculationHomeState

    private val _salaryCalculationState = MutableLiveData<PageLoadState<SalaryCalculationResponse>>()
    val salaryCalculationState: LiveData<PageLoadState<SalaryCalculationResponse>> =
        _salaryCalculationState

    private val _overtimeCalculationState =
        MutableLiveData<PageLoadState<OvertimeCalculationResponse>>()
    val overtimeCalculationState: LiveData<PageLoadState<OvertimeCalculationResponse>> =
        _overtimeCalculationState

    private val _salaryDataState = MutableLiveData<PageLoadState<SetSalaryResponse>>()
    val salaryDataState: LiveData<PageLoadState<SetSalaryResponse>> = _salaryDataState

    private val _workHoursCalculationState =
        MutableLiveData<PageLoadState<WorkHoursCalculationResponse>>()
    val workHoursCalculationState: LiveData<PageLoadState<WorkHoursCalculationResponse>> =
        _workHoursCalculationState

    private val _taxCalculationState = MutableLiveData<PageLoadState<TaxCalculationResponse>>()
    val taxCalculationState: LiveData<PageLoadState<TaxCalculationResponse>> =
        _taxCalculationState

    private val _bonusCalculationState = MutableLiveData<PageLoadState<BonusCalculationResponse>>()
    val bonusCalculationState: LiveData<PageLoadState<BonusCalculationResponse>> =
        _bonusCalculationState

    private val _savingsCalculationState =
        MutableLiveData<PageLoadState<SavingsCalculationResponse>>()
    val savingsCalculationState: LiveData<PageLoadState<SavingsCalculationResponse>> =
        _savingsCalculationState

    fun getCalculationHomeData(request: CalculationHomeRequest = CalculationHomeRequest()) {
        _calculationHomeState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getCalculationHomeData(request)
        }.onSuccess { homeData ->
            _calculationHomeState.value = homeData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _calculationHomeState.value = PageLoadState.Error
            false
        }
    }

    fun getCalSalary(request: SalaryCalculationRequest) {
        _salaryCalculationState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getCalSalary(request)
        }.onSuccess { salaryData ->
            _salaryCalculationState.value = salaryData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _salaryCalculationState.value = PageLoadState.Error
            false
        }
    }

    fun getSalaryData() {
        _salaryDataState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getSalaryData()
        }.onSuccess { salaryData ->
            _salaryDataState.value = salaryData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _salaryDataState.value = PageLoadState.Error
            false
        }
    }

    fun getCalOvertime(request: OvertimeCalculationRequest) {
        _overtimeCalculationState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getCalOvertime(request)
        }.onSuccess { overtimeData ->
            _overtimeCalculationState.value = overtimeData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _overtimeCalculationState.value = PageLoadState.Error
            false
        }
    }

    fun getCalWorkHours(request: WorkHoursCalculationRequest) {
        _workHoursCalculationState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getCalWorkHours(request)
        }.onSuccess { workHoursData ->
            _workHoursCalculationState.value = workHoursData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _workHoursCalculationState.value = PageLoadState.Error
            false
        }
    }

    fun getCalTax(request: TaxCalculationRequest) {
        _taxCalculationState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getCalTax(request)
        }.onSuccess { taxData ->
            _taxCalculationState.value = taxData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _taxCalculationState.value = PageLoadState.Error
            false
        }
    }

    fun getCalBonus(request: BonusCalculationRequest) {
        _bonusCalculationState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getCalBonus(request)
        }.onSuccess { bonusData ->
            _bonusCalculationState.value = bonusData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _bonusCalculationState.value = PageLoadState.Error
            false
        }
    }

    fun getCalSavings(request: SavingsCalculationRequest) {
        _savingsCalculationState.value = PageLoadState.Loading
        createNetworkRequest {
            repository.getCalSavings(request)
        }.onSuccess { savingsData ->
            _savingsCalculationState.value = savingsData?.let { PageLoadState.Content(it) }
                ?: PageLoadState.Empty
        }.onFailed {
            _savingsCalculationState.value = PageLoadState.Error
            false
        }
    }
}
