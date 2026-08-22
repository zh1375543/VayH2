package com.velora.portal.moneyflow

import com.velora.portal.moneyflow.model.BonusCalculationRequest
import com.velora.portal.moneyflow.model.BonusCalculationResponse
import com.velora.portal.moneyflow.model.CalculationHomeRequest
import com.velora.portal.moneyflow.model.CalculationHomeResponse
import com.velora.portal.moneyflow.model.OvertimeCalculationRequest
import com.velora.portal.moneyflow.model.OvertimeCalculationResponse
import com.velora.portal.moneyflow.model.SetSalaryRequest
import com.velora.portal.moneyflow.model.SetSalaryResponse
import com.velora.portal.moneyflow.model.SalaryCalculationRequest
import com.velora.portal.moneyflow.model.SalaryCalculationResponse
import com.velora.portal.moneyflow.model.SavingsCalculationRequest
import com.velora.portal.moneyflow.model.SavingsCalculationResponse
import com.velora.portal.moneyflow.model.TaxCalculationRequest
import com.velora.portal.moneyflow.model.TaxCalculationResponse
import com.velora.portal.moneyflow.model.WorkHoursCalculationRequest
import com.velora.portal.moneyflow.model.WorkHoursCalculationResponse
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
