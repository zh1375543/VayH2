package com.velora.portal.platform.network

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
import com.velora.portal.calculation.model.SetSalaryRequest
import com.velora.portal.calculation.model.SetSalaryResponse
import com.velora.portal.calculation.model.TaxCalculationRequest
import com.velora.portal.calculation.model.TaxCalculationResponse
import com.velora.portal.calculation.model.WorkHoursCalculationRequest
import com.velora.portal.calculation.model.WorkHoursCalculationResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CalcuPageApi {

    @POST("api/user/app/income/home")
    suspend fun getCalculationHomeData(@Body param: CalculationHomeRequest): ServiceResponse<CalculationHomeResponse?>

    @POST("api/user/app/income/salary/save")
    suspend fun saveSalaryData(@Body param: SetSalaryRequest): ServiceResponse<SetSalaryResponse?>


    @POST("api/user/app/income/salary/info")
    suspend fun getSalaryData(@Body param: ApiRequest): ServiceResponse<SetSalaryResponse?>

    @POST("api/user/app/income/calculator/salary")
    suspend fun  getCalSalary(@Body param: SalaryCalculationRequest):ServiceResponse<SalaryCalculationResponse?>

    @POST("api/user/app/income/calculator/overtime")
    suspend fun  getCalOvertime(@Body param: OvertimeCalculationRequest):ServiceResponse<OvertimeCalculationResponse?>

    @POST("api/user/app/income/calculator/work-hours")
    suspend fun  getCalWorkhours(@Body param: WorkHoursCalculationRequest):ServiceResponse<WorkHoursCalculationResponse?>

    @POST("api/user/app/income/calculator/tax")
    suspend fun  getCalTaxData(@Body param: TaxCalculationRequest):ServiceResponse<TaxCalculationResponse?>

    @POST("api/user/app/income/calculator/bonus")
    suspend fun  getCalTaxData(@Body param: BonusCalculationRequest):ServiceResponse<BonusCalculationResponse?>

    @POST("api/user/app/income/calculator/savings")
    suspend fun  getCalSvaingData(@Body param: SavingsCalculationRequest):ServiceResponse<SavingsCalculationResponse?>


}
