package com.novexa.platform.calculation

import com.novexa.platform.calculation.model.BonusCalculationRequest
import com.novexa.platform.calculation.model.BonusCalculationResponse
import com.novexa.platform.calculation.model.CalculationHomeRequest
import com.novexa.platform.calculation.model.CalculationHomeResponse
import com.novexa.platform.calculation.model.OvertimeCalculationRequest
import com.novexa.platform.calculation.model.OvertimeCalculationResponse
import com.novexa.platform.calculation.model.SetSalaryRequest
import com.novexa.platform.calculation.model.SetSalaryResponse
import com.novexa.platform.calculation.model.SalaryCalculationRequest
import com.novexa.platform.calculation.model.SalaryCalculationResponse
import com.novexa.platform.calculation.model.SavingsCalculationRequest
import com.novexa.platform.calculation.model.SavingsCalculationResponse
import com.novexa.platform.calculation.model.TaxCalculationRequest
import com.novexa.platform.calculation.model.TaxCalculationResponse
import com.novexa.platform.calculation.model.WorkHoursCalculationRequest
import com.novexa.platform.calculation.model.WorkHoursCalculationResponse
import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.network.CalcuPageApi

class SideHomeRepository(
    private val api: CalcuPageApi = NetworkProvider.calcuPageApi,
) {

    suspend fun getCalculationHomeData(
        request: CalculationHomeRequest = CalculationHomeRequest(),
    ): CalculationHomeResponse? {
        return api.getCalculationHomeData(request).dataOrThrow()
    }

    suspend fun saveSalaryData(request: SetSalaryRequest): SetSalaryResponse? {
        return api.saveSalaryData(request).dataOrThrow()
    }

    suspend fun getSalaryData(request: ApiRequest = ApiRequest()): SetSalaryResponse? {
        return api.getSalaryData(request).dataOrThrow()
    }

    suspend fun getCalSalary(request: SalaryCalculationRequest): SalaryCalculationResponse? {
        return api.getCalSalary(request).dataOrThrow()
    }

    suspend fun getCalOvertime(request: OvertimeCalculationRequest): OvertimeCalculationResponse? {
        return api.getCalOvertime(request).dataOrThrow()
    }

    suspend fun getCalWorkHours(
        request: WorkHoursCalculationRequest,
    ): WorkHoursCalculationResponse? {
        return api.getCalWorkhours(request).dataOrThrow()
    }

    suspend fun getCalTax(request: TaxCalculationRequest): TaxCalculationResponse? {
        return api.getCalTaxData(request).dataOrThrow()
    }

    suspend fun getCalBonus(request: BonusCalculationRequest): BonusCalculationResponse? {
        return api.getCalTaxData(request).dataOrThrow()
    }

    suspend fun getCalSavings(request: SavingsCalculationRequest): SavingsCalculationResponse? {
        return api.getCalSvaingData(request).dataOrThrow()
    }
}
