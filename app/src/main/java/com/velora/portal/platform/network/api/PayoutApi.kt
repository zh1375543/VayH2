package com.velora.portal.platform.network.api

import com.velora.portal.domain.payout.model.AccountChannelResponse
import com.velora.portal.domain.payout.model.AccountMethodResponse
import com.velora.portal.domain.payout.model.LinkedAccountResponse
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PayoutApi {
    @POST("api/user/app/bank/list")
    suspend fun loadPayoutChannels(@Body paramBean: ApiRequest): ServiceResponse<MutableList<AccountChannelResponse>?>

    @POST("api/user/app/wallet/list")
    suspend fun loadWalletAccounts(@Body param: ApiRequest): ServiceResponse<MutableList<AccountMethodResponse>?>

    @POST("api/user/app/userCashWallet/list/my")
    suspend fun loadMyWallets(@Body param: ApiRequest): ServiceResponse<MutableList<AccountMethodResponse>?>

    @POST("api/user/app/userCashWallet/set/default")
    suspend fun markDefaultWallet(@Body param: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/bind")
    suspend fun linkBankCard(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/myCard")
    suspend fun loadLinkedCards(@Body paramBean: ApiRequest): ServiceResponse<MutableList<LinkedAccountResponse>?>

    @POST("api/user/app/bank/addBank")
    suspend fun attachBankCard(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/unbind")
    suspend fun detachBankCard(@Body paramBean: ApiRequest): ServiceResponse<Any?>

    @POST("api/user/app/bank/setDefault")
    suspend fun setDefaultCard(@Body paramBean: ApiRequest): ServiceResponse<Any?>
}
