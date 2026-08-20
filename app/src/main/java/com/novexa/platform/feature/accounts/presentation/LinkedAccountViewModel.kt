package com.novexa.platform.feature.accounts.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.feature.accounts.data.LinkedAccountRepository
import com.novexa.platform.feature.accounts.model.LinkedAccountResponse
import com.novexa.platform.feature.accounts.model.AccountChannelResponse
import com.novexa.platform.feature.accounts.model.AccountMethodResponse
import com.novexa.platform.core.common.util.PageLoadState

class LinkedAccountViewModel(
    private val walletRepository: LinkedAccountRepository = LinkedAccountRepository(),
) : BaseViewModel() {

    var payChannelList = MutableLiveData<List<AccountChannelResponse>?>()
    fun getPayChannelList() {
        if (payChannelList.value != null) {
            payChannelList.value = payChannelList.value
            return
        }
        createNetworkRequest {
            walletRepository.fetchPayChannelList()
        }.showLoading().onSuccess {
            payChannelList.value = it
        }.execute()
    }

    val walletList = MutableLiveData<List<AccountMethodResponse>?>()
    fun getWalletList() {
        if (walletList.value != null) {
            walletList.value = walletList.value
            return
        }
        createNetworkRequest {
            walletRepository.fetchWalletList()
        }.showLoading().onSuccess {
            walletList.value = it
        }.execute()
    }

    val addResult = MutableLiveData<Any?>()
    fun addCard(
        bankId: String?,
        accountUser: String,
        bankNo: String,
        payWay: String = "CARD",
        walletId: Int? = null,
        accountCode: String? = null,
    ) {
        createNetworkRequest {
            walletRepository.addCard(
                bankId = bankId,
                accountUser = accountUser,
                bankNo = bankNo,
                payWay = payWay,
                walletId = walletId,
                accountCode = accountCode,
            )
        }.showLoading().onSuccess {
            addResult.value = it
        }.execute()
    }

    val bankCardListResult = MutableLiveData<List<LinkedAccountResponse>>()
    fun getBankcardList(errorAction: () -> Unit) {
        createNetworkRequest {
            walletRepository.fetchBankcardList()
        }.onSuccess {
            bankCardListResult.value = it
        }.onFailed {
            errorAction()
            true
        }
    }

    private val _accountListState = MutableLiveData<PageLoadState<List<LinkedAccountResponse>>>(
        PageLoadState.Loading,
    )
    val accountListState: LiveData<PageLoadState<List<LinkedAccountResponse>>> = _accountListState

    fun getAccountList() {
        _accountListState.value = PageLoadState.Loading
        createNetworkRequest {
            walletRepository.fetchBankcardList()
        }.onSuccess { cards ->
            loadWalletAccounts(cards.orEmpty())
        }.onFailed {
            _accountListState.value = PageLoadState.Error
            true
        }
    }

    private fun loadWalletAccounts(
        cards: List<LinkedAccountResponse>,
    ) {
        createNetworkRequest {
            walletRepository.fetchMyWalletList()
        }.onSuccess { wallets ->
            val bankAccounts = cards.map { card ->
                card.copy(payWay = "CARD")
            }
            val walletAccounts = wallets.orEmpty().map { wallet ->
                wallet.toBankAccountResponse()
            }
            val accounts = bankAccounts + walletAccounts
            _accountListState.value = if (accounts.isEmpty()) {
                PageLoadState.Empty
            } else {
                PageLoadState.Content(accounts)
            }
        }.onFailed {
            _accountListState.value = PageLoadState.Error
            true
        }
    }

    val loanAccountList = MutableLiveData<List<LinkedAccountResponse>>()
    fun getLoanAccountList(errorAction: () -> Unit) {
        createNetworkRequest {
            walletRepository.fetchMyWalletList()
        }.onSuccess { wallets ->
            loadLoanBankAccounts(wallets.orEmpty(), errorAction)
        }.onFailed {
            loadLoanBankAccounts(emptyList(), errorAction)
            true
        }
    }

    private fun loadLoanBankAccounts(
        wallets: List<AccountMethodResponse>,
        errorAction: () -> Unit,
    ) {
        createNetworkRequest {
            walletRepository.fetchBankcardList()
        }.onSuccess { cards ->
            val walletAccounts = wallets.map { wallet -> wallet.toBankAccountResponse() }
            val bankAccounts = cards.orEmpty().map { card ->
                card.copy(payWay = "CARD")
            }
            loanAccountList.value = walletAccounts + bankAccounts
        }.onFailed {
            errorAction()
            true
        }
    }

    fun unBindCard(id: String, payWay: String = "CARD", action: () -> Unit) {
        createNetworkRequest {
            walletRepository.unbindCard(id, payWay)
        }.showLoading().onSuccess {
            action.invoke()
        }.execute()
    }

    fun setDefaultCard(id: String, action: () -> Unit) {
        createNetworkRequest {
            walletRepository.setDefaultCard(id)
        }.showLoading().onSuccess {
            action.invoke()
        }.execute()
    }

    fun setDefaultWallet(id: Int?, action: () -> Unit) {
        createNetworkRequest {
            walletRepository.setDefaultWallet(id)
        }.showLoading().onSuccess {
            action.invoke()
        }.execute()
    }

    private fun AccountMethodResponse.toBankAccountResponse() = LinkedAccountResponse(
        id = id.toLong(),
        bankNo = accountCode,
        bankName = walletName.orEmpty(),
        isDefault = defaultSign ?: 0,
        payWay = "WALLET",
    )
}
