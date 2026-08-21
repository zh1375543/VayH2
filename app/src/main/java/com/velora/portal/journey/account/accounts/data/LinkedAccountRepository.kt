package com.velora.portal.journey.account.accounts.data

import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.domain.payout.model.LinkedAccountResponse
import com.velora.portal.domain.payout.model.AccountChannelResponse
import com.velora.portal.domain.payout.model.AccountMethodResponse

class LinkedAccountRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun loadPayoutChannels(): List<AccountChannelResponse>? {
        return api.loadPayoutChannels(ApiRequest()).dataOrThrow()
    }

    suspend fun loadWalletMethods(): List<AccountMethodResponse>? {
        return api.loadWalletAccounts(ApiRequest()).dataOrThrow()
    }

    suspend fun loadLinkedWallets(): List<AccountMethodResponse>? {
        return api.loadMyWallets(ApiRequest()).dataOrThrow()
    }

    suspend fun linkNewCard(
        bankId: String?,
        accountUser: String,
        bankNo: String,
        payWay: String = "CARD",
        walletId: Int? = null,
        accountCode: String? = null,
    ): Any? {
        return api.attachBankCard(
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

    suspend fun loadLinkedCards(): List<LinkedAccountResponse>? {
        return api.loadLinkedCards(ApiRequest()).dataOrThrow()
    }

    suspend fun unlinkBankCard(bankInfoId: String, payWay: String = "CARD"): Any? {
        return api.detachBankCard(
            ApiRequest(
                bankInfoId = bankInfoId,
                payWay = payWay,
            )
        ).dataOrThrow()
    }

    suspend fun markDefaultCard(bankInfoId: String): Any? {
        return api.markDefaultCard(ApiRequest(bankInfoId = bankInfoId)).dataOrThrow()
    }

    suspend fun markDefaultWallet(id: Int?): Any? {
        return api.markDefaultWallet(ApiRequest(id = id)).dataOrThrow()
    }
}
