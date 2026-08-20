package com.novexa.platform.feature.accounts.data

import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.feature.accounts.model.LinkedAccountResponse
import com.novexa.platform.feature.accounts.model.AccountChannelResponse
import com.novexa.platform.feature.accounts.model.AccountMethodResponse

class LinkedAccountRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun fetchPayChannelList(): List<AccountChannelResponse>? {
        return api.fetchPayChannel(ApiRequest()).dataOrThrow()
    }

    suspend fun fetchWalletList(): List<AccountMethodResponse>? {
        return api.getWalletList(ApiRequest()).dataOrThrow()
    }

    suspend fun fetchMyWalletList(): List<AccountMethodResponse>? {
        return api.getMyWalletList(ApiRequest()).dataOrThrow()
    }

    suspend fun addCard(
        bankId: String?,
        accountUser: String,
        bankNo: String,
        payWay: String = "CARD",
        walletId: Int? = null,
        accountCode: String? = null,
    ): Any? {
        return api.addCard(
            ApiRequest(
                bankId = bankId,
                accountUser = accountUser,
                bankNo = bankNo,
                payWay = payWay,
                walletId = walletId,
                accountCode = accountCode,
            )
        ).dataOrThrow()
    }

    suspend fun fetchBankcardList(): List<LinkedAccountResponse>? {
        return api.fetchBankcardList(ApiRequest()).dataOrThrow()
    }

    suspend fun unbindCard(bankInfoId: String, payWay: String = "CARD"): Any? {
        return api.unbindCard(
            ApiRequest(
                bankInfoId = bankInfoId,
                payWay = payWay,
            )
        ).dataOrThrow()
    }

    suspend fun setDefaultCard(bankInfoId: String): Any? {
        return api.fetchCardDefault(ApiRequest(bankInfoId = bankInfoId)).dataOrThrow()
    }

    suspend fun setDefaultWallet(id: Int?): Any? {
        return api.setDefaultWallet(ApiRequest(id = id)).dataOrThrow()
    }
}
