package com.velora.portal.platform.network.api

import com.velora.portal.domain.customer.model.AccessSessionResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/user/app/authenticate/sms")
    suspend fun requestOtpCode(@Body param: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/authenticate")
    suspend fun authenticate(@Body param: ApiRequest): ServiceResponse<AccessSessionResponse?>

    @POST("api/user/app/delete/user")
    suspend fun signOut(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/password/set")
    suspend fun establishPin(@Body param: ApiRequest): ServiceResponse<AccessSessionResponse?>

    @POST("api/user/app/password/update")
    suspend fun refreshPassword(@Body param: ApiRequest): ServiceResponse<AccessSessionResponse?>
}
