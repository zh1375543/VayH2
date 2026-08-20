package com.novexa.platform.core.network

import com.novexa.platform.calculation.model.BonusCalculationRequest
import com.novexa.platform.calculation.model.BonusCalculationResponse
import com.novexa.platform.calculation.model.CalculationHomeRequest
import com.novexa.platform.calculation.model.CalculationHomeResponse
import com.novexa.platform.calculation.model.OvertimeCalculationRequest
import com.novexa.platform.calculation.model.OvertimeCalculationResponse
import com.novexa.platform.calculation.model.SalaryCalculationRequest
import com.novexa.platform.calculation.model.SalaryCalculationResponse
import com.novexa.platform.calculation.model.SavingsCalculationRequest
import com.novexa.platform.calculation.model.SavingsCalculationResponse
import com.novexa.platform.calculation.model.SetSalaryRequest
import com.novexa.platform.calculation.model.SetSalaryResponse
import com.novexa.platform.calculation.model.TaxCalculationRequest
import com.novexa.platform.calculation.model.TaxCalculationResponse
import com.novexa.platform.calculation.model.WorkHoursCalculationRequest
import com.novexa.platform.calculation.model.WorkHoursCalculationResponse
import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.common.data.bean.ServiceResponse
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
