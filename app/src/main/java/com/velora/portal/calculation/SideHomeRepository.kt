package com.velora.portal.calculation

import com.velora.portal.calculation.model.BonusCalculationRequest
import com.velora.portal.calculation.model.BonusCalculationResponse
import com.velora.portal.calculation.model.CalculationHomeRequest
import com.velora.portal.calculation.model.CalculationHomeResponse
import com.velora.portal.calculation.model.OvertimeCalculationRequest
import com.velora.portal.calculation.model.OvertimeCalculationResponse
import com.velora.portal.calculation.model.SetSalaryRequest
import com.velora.portal.calculation.model.SetSalaryResponse
import com.velora.portal.calculation.model.SalaryCalculationRequest
import com.velora.portal.calculation.model.SalaryCalculationResponse
import com.velora.portal.calculation.model.SavingsCalculationRequest
import com.velora.portal.calculation.model.SavingsCalculationResponse
import com.velora.portal.calculation.model.TaxCalculationRequest
import com.velora.portal.calculation.model.TaxCalculationResponse
import com.velora.portal.calculation.model.WorkHoursCalculationRequest
import com.velora.portal.calculation.model.WorkHoursCalculationResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.network.CalcuPageApi

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
