package com.velora.portal.feature.accounts.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.feature.accounts.model.AccountChannelResponse
import com.velora.portal.feature.accounts.model.AccountMethodResponse
import com.velora.portal.feature.accounts.presentation.dialog.chooseBankDialog
import com.velora.portal.feature.accounts.presentation.dialog.chooseWalletDialog
import com.velora.portal.feature.accounts.presentation.dialog.showWithdrawMethodDialog
import com.velora.portal.databinding.ActivityLinkedAccountSetupBinding
import com.velora.portal.core.ui.extension.observeKeyboardVisibility
import com.velora.portal.core.ui.extension.resetScale
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.feature.onboarding.presentation.profile.ApplicantDetailsViewModel
import com.velora.portal.core.common.util.showToastMessage
import com.velora.portal.core.common.util.viewBinding

class LinkedAccountSetupActivity : BaseActivity<ActivityLinkedAccountSetupBinding>() {

    private enum class WithdrawMethod {
        BANK,
        WALLET,
    }

    override val binding by viewBinding(ActivityLinkedAccountSetupBinding::inflate)

    private val vm by viewModels<LinkedAccountViewModel>()
    private val personalVm by viewModels<ApplicantDetailsViewModel>()

    private var selectedWithdrawMethod: WithdrawMethod? = null
    private var bankBean: AccountChannelResponse? = null
    private var walletBean: AccountMethodResponse? = null
    private var shouldShowWalletPicker = false

    override fun initView() {
        bindWithdrawFieldEvents()
        bindAccountValidators()
        handleKeyboardVisibility()
        setupNextButtonAndPrefill()
    }

    /** Wires up the withdraw-method selector fields and clears any previous selection. */
    private fun bindWithdrawFieldEvents() = with(binding.withdrawAccountForm) {
        clearWithdrawMethodSelection()

        disbursementMethodField.setOnClick {
            showWithdrawMethodDialog(
                walletAction = { selectDefaultWallet() },
                bankAction = { vm.getPayChannelList() },
            )
        }
        bankSelectorField.setOnClick { vm.getPayChannelList() }
        walletProviderField.setOnClick {
            shouldShowWalletPicker = true
            vm.getWalletList()
        }
    }

    /** Validates account numbers against their confirmation fields as the user types. */
    private fun bindAccountValidators() = with(binding.withdrawAccountForm) {
        bankAccountNumberField.getEditText().doAfterTextChanged {
            bankAccountNumberField.hideError()
            if (it.toString() == bankAccountConfirmationField.getText()) bankAccountConfirmationField.hideError()
        }
        bankAccountConfirmationField.getEditText().doAfterTextChanged {
            if (it.toString() == bankAccountNumberField.getText()) {
                bankAccountNumberField.hideError()
                bankAccountConfirmationField.hideError()
            }
        }
        walletAccountNumberField.getEditText().doAfterTextChanged {
            walletAccountNumberField.hideError()
            if (it.toString() == walletAccountConfirmationField.getText()) {
                walletAccountConfirmationField.hideError()
            }
        }
        walletAccountConfirmationField.getEditText().doAfterTextChanged {
            if (it.toString() == walletAccountNumberField.getText()) {
                walletAccountNumberField.hideError()
                walletAccountConfirmationField.hideError()
            }
        }
    }

    /** Toggles the tips visibility while the soft keyboard is shown or hidden. */
    private fun handleKeyboardVisibility() = with(binding) {
        window.decorView.observeKeyboardVisibility { isShow, _ ->
            if (isShow) {
                tvTips.isVisible = false
            } else {
                tvTips.postDelayed({ tvTips.isVisible = true }, 200)
            }
        }
    }

    /** Prepares the submit button and pre-fills the account holder from personal info. */
    private fun setupNextButtonAndPrefill() = with(binding) {
        tvNext.resetScale()
        tvNext.singleClick {
            when (selectedWithdrawMethod) {
                WithdrawMethod.BANK -> submitBankAccount()
                WithdrawMethod.WALLET -> submitWalletAccount()
                null -> {
                    withdrawAccountForm.disbursementMethodField.showError()
                }
            }
        }

        personalVm.getPersonalInfo {}
    }

    private fun submitBankAccount() {
        with(binding.withdrawAccountForm) {
            if (bankSelectorField.getText().isBlank()) {
                bankSelectorField.showError()
                return
            }
            if (accountHolderField.getText().isBlank()) {
                accountHolderField.showError()
                return
            }
            if (bankAccountNumberField.getText().isBlank()) {
                bankAccountNumberField.showError()
                return
            }
            if (bankAccountConfirmationField.getText() != bankAccountNumberField.getText()) {
                bankAccountConfirmationField.showError()
                return
            }
            vm.addCard(
                bankId = bankBean?.id?.toString(),
                accountUser = accountHolderField.getText(),
                bankNo = bankAccountNumberField.getText(),
            )
        }
    }

    private fun submitWalletAccount() {
        with(binding.withdrawAccountForm) {
            if (walletProviderField.getText().isBlank()) {
                walletProviderField.showError()
                return
            }
            if (walletAccountNumberField.getText().isBlank()) {
                walletAccountNumberField.showError()
                return
            }
            if (walletAccountConfirmationField.getText() != walletAccountNumberField.getText()) {
                walletAccountConfirmationField.showError()
                return
            }
            vm.addCard(
                bankId = null,
                accountUser = "",
                bankNo = "",
                payWay = "WALLET",
                walletId = walletBean?.id,
                accountCode = walletAccountNumberField.getText().trim(),
            )
        }
    }

    private fun selectWithdrawMethod(method: WithdrawMethod) = with(binding.withdrawAccountForm) {
        selectedWithdrawMethod = method
        disbursementMethodField.setText(getString(
            if (method == WithdrawMethod.BANK) R.string.bank else R.string.e_wallet,
        ))
        disbursementMethodField.hideError()
        bankDetailsSection.isVisible = method == WithdrawMethod.BANK
        walletDetailsSection.isVisible = method == WithdrawMethod.WALLET
    }

    private fun clearWithdrawMethodSelection() = with(binding.withdrawAccountForm) {
        selectedWithdrawMethod = null
        shouldShowWalletPicker = false
        walletBean = null
        disbursementMethodField.setText(null)
        disbursementMethodField.hideError()
        bankDetailsSection.isVisible = false
        walletDetailsSection.isVisible = false
    }

    private fun selectDefaultWallet() = with(binding.withdrawAccountForm) {
        shouldShowWalletPicker = false
        walletBean = null
        selectWithdrawMethod(WithdrawMethod.WALLET)
        walletProviderField.setText(getString(R.string.gcash))
        walletProviderField.hideError()
        vm.getWalletList()
    }

    private fun applyWalletSelection(wallet: AccountMethodResponse) = with(binding.withdrawAccountForm) {
        selectWithdrawMethod(WithdrawMethod.WALLET)
        walletProviderField.setText(wallet.walletName)
        walletProviderField.hideError()
        walletBean = wallet
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        payChannelList.observe(this@LinkedAccountSetupActivity) {
            chooseBankDialog(it ?: emptyList()) { bean ->
                selectWithdrawMethod(WithdrawMethod.BANK)
                binding.withdrawAccountForm.bankSelectorField.setText(bean.bankName)
                binding.withdrawAccountForm.bankSelectorField.hideError()
                bankBean = bean
            }
        }
        walletList.observe(this@LinkedAccountSetupActivity) {
            val walletItems = it ?: emptyList()
            if (shouldShowWalletPicker) {
                shouldShowWalletPicker = false
                chooseWalletDialog(walletItems) { wallet ->
                    applyWalletSelection(wallet)
                }
            } else if (selectedWithdrawMethod == WithdrawMethod.WALLET) {
                walletBean = walletItems.firstOrNull {
                    it.walletName.equals(getString(R.string.gcash), ignoreCase = true)
                }
            }
        }
        addResult.observe(this@LinkedAccountSetupActivity) {
            getString(R.string.toast_add_account_receivable).showToastMessage()
            finish()
        }
        personalVm.personalResult.observe(this@LinkedAccountSetupActivity) {
            binding.withdrawAccountForm.accountHolderField.setText(it?.cardName)
        }
    }
}
