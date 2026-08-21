package com.velora.portal.feature.accounts.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.feature.accounts.model.LinkedAccountResponse
import com.velora.portal.feature.accounts.model.AccountChannelResponse
import com.velora.portal.feature.accounts.model.AccountMethodResponse

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
