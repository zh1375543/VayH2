package com.velora.portal.journey.access.presentation.contact

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.provider.ContactsContract
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.velora.portal.R
import com.velora.portal.application.MainApplication
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenContactPayoutBinding
import com.velora.portal.platform.common.data.ACT_clickBack
import com.velora.portal.platform.common.data.ACT_clickContinue
import com.velora.portal.platform.common.data.ACT_clickNext
import com.velora.portal.platform.common.data.ACT_in
import com.velora.portal.platform.common.data.ACT_inputBankAccountEnd
import com.velora.portal.platform.common.data.ACT_inputBankAccountStart
import com.velora.portal.platform.common.data.ACT_inputNameEnd
import com.velora.portal.platform.common.data.ACT_inputNameStart
import com.velora.portal.platform.common.data.ACT_InputPhoneNumberEnd
import com.velora.portal.platform.common.data.ACT_InputPhonenumberStart
import com.velora.portal.platform.common.data.ACT_selectContactName1End
import com.velora.portal.platform.common.data.ACT_selectContactName1Start
import com.velora.portal.platform.common.data.ACT_selectContactName2End
import com.velora.portal.platform.common.data.ACT_selectContactName2Start
import com.velora.portal.platform.common.data.ACT_selectContactName3End
import com.velora.portal.platform.common.data.ACT_selectContactName3Start
import com.velora.portal.platform.common.data.PageInfoBank
import com.velora.portal.platform.common.data.authConfigList
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.domain.payout.model.AccountChannelResponse
import com.velora.portal.domain.customer.model.RelativesBean
import com.velora.portal.domain.customer.model.EmploymentContactResponse
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.journey.access.presentation.AuthStatusViewModel
import com.velora.portal.journey.access.presentation.routeToNextAuthStep
import com.velora.portal.journey.access.presentation.profile.ApplicantDetailsViewModel
import com.velora.portal.domain.payout.model.AccountMethodResponse
import com.velora.portal.journey.account.accounts.presentation.LinkedAccountViewModel
import com.velora.portal.platform.common.util.SUPPLEMENTARY_INFO_COMMIT
import com.velora.portal.platform.design.extension.resetScale
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.PermissionScenario
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.journey.account.accounts.presentation.dialog.chooseBankDialog
import com.velora.portal.journey.account.accounts.presentation.dialog.chooseWalletDialog
import com.velora.portal.platform.design.dialog.showConfirmDialog
import com.velora.portal.journey.account.accounts.presentation.dialog.showWithdrawMethodDialog
import com.velora.portal.platform.design.dialog.showOptionPickerDialog
import com.velora.portal.platform.common.util.getContactInfo
import com.velora.portal.platform.common.util.viewBinding
import kotlin.math.max
import kotlin.toString

class ContactPayoutActivity : BaseActivity<ScreenContactPayoutBinding>() {

    private enum class WithdrawMethod {
        BANK,
        WALLET,
    }

    private enum class ContactPickTarget {
        PRIMARY,
        SECONDARY,
        ADDITIONAL,
    }

    override val binding by viewBinding(ScreenContactPayoutBinding::inflate)

    private val vm by viewModels<WorkContactViewModel>()
    private val personalVm by viewModels<ApplicantDetailsViewModel>()
    private val homeVm by viewModels<AuthStatusViewModel>()
    private val accountVm by viewModels<LinkedAccountViewModel>()
    private val isCert by lazy { intent.getBooleanExtra("isCert", false) }
    private var shouldShowBottomAction = false
    private var selectedWithdrawMethod: WithdrawMethod? = null
    private var shouldShowWalletPicker = false

    override fun shouldDismissKeyboardOnOutsideTouch(ev: MotionEvent): Boolean {
        val bottomActionBounds = Rect()
        binding.submissionActionBar.getGlobalVisibleRect(bottomActionBounds)
        return !bottomActionBounds.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }

    override fun initView() {
        preparePayoutAndContactForm()
        bindWithdrawalMethodFields()
        bindEmergencyContactFields()
        configurePageNavigationAndLoading()
    }

    /** Render the page mode, title and bottom action area for the current auth step. */
    private fun preparePayoutAndContactForm() = with(binding) {
        configureFormEditing(isEditable = !isCert)
        vm.submitTrackingEvent(TrackBean(p = PageInfoBank, act = ACT_in))
        payoutContactHeader.setAction("${authConfigList.indexOf("BANK") + 1}/${authConfigList.size}")
        clearWithdrawMethodSelection()
        setBottomActionVisible(false)
        payoutContactHeader.showAction(!isCert)
        payoutContactHeader.updateTitle(
            if (isCert) getString(R.string.contact_info) else getString(R.string.bank_and_contact),
        )
        if (!isCert) {
            btnContinue.resetScale()
        }
    }

    private fun configureFormEditing(isEditable: Boolean) = with(binding) {
        payoutMethodSection.isVisible = isEditable
        tvContactGuidance.isVisible = isEditable
        listOf(
            primaryRelationshipField,
            primaryContactNameField,
            primaryContactPhoneField,
            secondaryRelationshipField,
            secondaryContactNameField,
            secondaryContactPhoneField,
            additionalRelationshipField,
            additionalContactNameField,
            additionalContactPhoneField,
        ).forEach { field ->
            field.setEnableEdit(isEditable)
        }
        primaryContactNameField.setContactVisible(isEditable)
        secondaryContactNameField.setContactVisible(isEditable)
        additionalContactNameField.setContactVisible(isEditable)
    }

    /** Bind bank account and wallet input actions. */
    private fun bindWithdrawalMethodFields() = with(binding.disbursementForm) {
        disbursementMethodField.setOnClick {
            showWithdrawMethodDialog(
                walletAction = { selectDefaultWallet() },
                bankAction = { accountVm.getPayChannelList() },
            )
        }
        bankSelectorField.setOnClick { accountVm.getPayChannelList() }
        walletProviderField.setOnClick {
            shouldShowWalletPicker = true
            accountVm.getWalletList()
        }
        accountHolderField.getEditText().setOnFocusChangeListener { _, hasFocus ->
            if (isCert) return@setOnFocusChangeListener
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoBank,
                    act = if (hasFocus) ACT_inputNameStart else ACT_inputNameEnd,
                    result = System.currentTimeMillis().toString(),
                ),
            )
        }
        bankAccountNumberField.getEditText().doAfterTextChanged {
            bankAccountNumberField.hideError()
            if (it.toString() == bankAccountConfirmationField.getText()) {
                bankAccountConfirmationField.hideError()
            }
        }
        bankAccountNumberField.getEditText().setOnFocusChangeListener { _, hasFocus ->
            if (isCert) return@setOnFocusChangeListener
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoBank,
                    act = if (hasFocus) ACT_inputBankAccountStart else ACT_inputBankAccountEnd,
                    result = System.currentTimeMillis().toString(),
                ),
            )
        }
        bankAccountConfirmationField.getEditText().doAfterTextChanged {
            if (it.toString() == bankAccountNumberField.getText()) {
                bankAccountConfirmationField.hideError()
            }
        }
        bankAccountConfirmationField.getEditText().setOnFocusChangeListener { _, hasFocus ->
            if (isCert) return@setOnFocusChangeListener
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoBank,
                    act = if (hasFocus) ACT_inputBankAccountStart else ACT_inputBankAccountEnd,
                    result = System.currentTimeMillis().toString(),
                ),
            )
        }
        walletAccountNumberField.getEditText().doAfterTextChanged {
            walletAccountNumberField.hideError()
            if (it.toString() == walletAccountConfirmationField.getText()) {
                walletAccountConfirmationField.hideError()
            }
        }
        walletAccountConfirmationField.getEditText().doAfterTextChanged {
            if (it.toString() == walletAccountNumberField.getText()) {
                walletAccountConfirmationField.hideError()
            }
        }
    }

    /** Bind relationship selectors and system contact pickers. */
    private fun bindEmergencyContactFields() = with(binding) {
        primaryRelationshipField.setOnClick {
            vm.getContactEnum {
                val relativesList = it.relatives ?: arrayListOf()
                showOptionPickerDialog(
                    relativesList.indexOfFirst { item -> primaryRelationshipField.getText() == item.info },
                    relativesList,
                ) { index ->
                    primaryRelationshipField.setText(relativesList[index].info)
                    primaryRelationshipField.hideError()
                    relativesStatus = relativesList[index].state
                }
            }
        }
        secondaryRelationshipField.setOnClick {
            vm.getContactEnum {
                val relativesList = it.otherRelatives ?: arrayListOf()
                showOptionPickerDialog(
                    relativesList.indexOfFirst { item -> secondaryRelationshipField.getText() == item.info },
                    relativesList,
                ) { index ->
                    secondaryRelationshipField.setText(relativesList[index].info)
                    secondaryRelationshipField.hideError()
                    friendStatus = relativesList[index].state
                }
            }
        }
        additionalRelationshipField.setOnClick {
            vm.getContactEnum {
                val relationshipOptions = it.otherRelatives ?: arrayListOf()
                showOptionPickerDialog(
                    relationshipOptions.indexOfFirst {
                        additionalRelationshipField.getText() == it.info
                    },
                    relationshipOptions,
                ) { index ->
                    additionalRelationshipField.setText(relationshipOptions[index].info)
                    additionalRelationshipField.hideError()
                    additionalContactStatus = relationshipOptions[index].state
                }
            }
        }
        primaryContactNameField.setContactClick {
            recordContactPickStart(ACT_selectContactName1Start, ContactPickTarget.PRIMARY)
        }
        primaryContactPhoneField.getEditText().setOnFocusChangeListener { _, hasFocus ->
            if (isCert) return@setOnFocusChangeListener
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoBank,
                    act = if (hasFocus) ACT_InputPhonenumberStart else ACT_InputPhoneNumberEnd,
                    result = System.currentTimeMillis().toString(),
                ),
            )
        }
        secondaryContactNameField.setContactClick {
            recordContactPickStart(ACT_selectContactName2Start, ContactPickTarget.SECONDARY)
        }
        secondaryContactPhoneField.getEditText().setOnFocusChangeListener { _, hasFocus ->
            if (isCert) return@setOnFocusChangeListener
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoBank,
                    act = if (hasFocus) ACT_InputPhonenumberStart else ACT_InputPhoneNumberEnd,
                    result = System.currentTimeMillis().toString(),
                ),
            )
        }
        additionalContactNameField.setContactClick {
            recordContactPickStart(ACT_selectContactName3Start, ContactPickTarget.ADDITIONAL)
        }
    }

    /** Connect navigation, validation, permission-gated submission and initial data loading. */
    private fun configurePageNavigationAndLoading() = with(binding) {
        payoutContactHeader.setNavigationAction { confirmPayoutSetupExit() }
        registerTrackedBackHandler(vm) { confirmPayoutSetupExit() }
        btnContinue.singleClick {
            if (!validateBankPage()) {
                return@singleClick
            }
            PermissionCoordinator.request(this@ContactPayoutActivity, PermissionScenario.DEVICE_RISK) {
                MainApplication.appViewModel.hasDeviceInfo(PageInfoBank) { isPost ->
                    if (isPost) {
                        submit()
                        return@hasDeviceInfo
                    }
                    MainApplication.appViewModel.postRiskInfo(PageInfoBank) { isSuccess ->
                        if (isSuccess) {
                            submit()
                        }
                    }
                }
            }
        }
        pageState.setOnRetryClickListener {
            setBottomActionVisible(false)
            pageContent.isVisible = false
            pageState.showLoading()
            vm.getContactsInfo {
                pageContent.isVisible = false
                pageState.showError()
            }
        }
        pageContent.isVisible = false
        pageState.showLoading()
        vm.getContactsInfo {
            pageContent.isVisible = false
            pageState.showError()
        }
        personalVm.getPersonalInfo {}
    }

    private fun validateBankPage(): Boolean = with(binding) {
        with(disbursementForm) {
            when (selectedWithdrawMethod) {
                WithdrawMethod.BANK -> {
                    if (bankSelectorField.getText().isBlank()) {
                        bankSelectorField.showError()
                        scrollToInvalidField(bankSelectorField)
                        return false
                    }
                    if (accountHolderField.getText().isBlank()) {
                        accountHolderField.showError()
                        scrollToInvalidField(accountHolderField)
                        return false
                    }
                    if (bankAccountNumberField.getText().isBlank()) {
                        bankAccountNumberField.showError()
                        scrollToInvalidField(bankAccountNumberField)
                        return false
                    }
                    if (bankAccountConfirmationField.getText() != bankAccountNumberField.getText()) {
                        bankAccountConfirmationField.showError()
                        scrollToInvalidField(bankAccountConfirmationField)
                        return false
                    }
                }
                WithdrawMethod.WALLET -> {
                    if (walletProviderField.getText().isBlank()) {
                        walletProviderField.showError()
                        scrollToInvalidField(walletProviderField)
                        return false
                    }
                    if (walletBean == null) {
                        walletProviderField.showError()
                        scrollToInvalidField(walletProviderField)
                        return false
                    }
                    if (walletAccountNumberField.getText().isBlank()) {
                        walletAccountNumberField.showError()
                        scrollToInvalidField(walletAccountNumberField)
                        return false
                    }
                    if (walletAccountConfirmationField.getText() != walletAccountNumberField.getText()) {
                        walletAccountConfirmationField.showError()
                        scrollToInvalidField(walletAccountConfirmationField)
                        return false
                    }
                }
                null -> {
                    disbursementMethodField.showError()
                    scrollToInvalidField(disbursementMethodField)
                    return false
                }
            }
        }
        if (primaryRelationshipField.getText().isBlank()) {
            primaryRelationshipField.showError()
            scrollToInvalidField(primaryRelationshipField)
            return false
        }
        if (primaryContactNameField.getText().isBlank()) {
            primaryContactNameField.showError()
            scrollToInvalidField(primaryContactNameField)
            return false
        }
        if (primaryContactPhoneField.getText().isBlank()) {
            primaryContactPhoneField.showError()
            scrollToInvalidField(primaryContactPhoneField)
            return false
        }
        if (secondaryRelationshipField.getText().isBlank()) {
            secondaryRelationshipField.showError()
            scrollToInvalidField(secondaryRelationshipField)
            return false
        }
        if (secondaryContactNameField.getText().isBlank()) {
            secondaryContactNameField.showError()
            scrollToInvalidField(secondaryContactNameField)
            return false
        }
        if (secondaryContactPhoneField.getText().isBlank()) {
            secondaryContactPhoneField.showError()
            scrollToInvalidField(secondaryContactPhoneField)
            return false
        }
        return true
    }

    /** Keeps the first invalid field fully visible above the keyboard-attached action area. */
    private fun scrollToInvalidField(target: View) = with(binding) {
        payoutContactScroll.post {
            val targetBounds = Rect().also(target::getDrawingRect)
            payoutContactScroll.offsetDescendantRectToMyCoords(target, targetBounds)

            val scrollLocation = IntArray(2)
            val actionLocation = IntArray(2)
            payoutContactScroll.getLocationOnScreen(scrollLocation)
            submissionActionBar.getLocationOnScreen(actionLocation)

            val spacing = resources.getDimensionPixelSize(R.dimen.dp_12)
            val viewportTop = payoutContactScroll.paddingTop + spacing
            val scrollBottom = payoutContactScroll.height - payoutContactScroll.paddingBottom - spacing
            val actionTop = actionLocation[1] - scrollLocation[1] - spacing
            val viewportBottom = minOf(scrollBottom, actionTop)
            if (viewportBottom <= viewportTop) return@post

            // targetBounds uses content coordinates; include scrollY in the visible bounds too.
            val visibleTop = payoutContactScroll.scrollY + viewportTop
            val visibleBottom = payoutContactScroll.scrollY + viewportBottom

            val availableHeight = visibleBottom - visibleTop
            val scrollDelta = when {
                targetBounds.height() > availableHeight -> targetBounds.top - visibleTop
                targetBounds.top < visibleTop -> targetBounds.top - visibleTop
                targetBounds.bottom > visibleBottom -> targetBounds.bottom - visibleBottom
                else -> 0
            }
            if (scrollDelta != 0) {
                payoutContactScroll.smoothScrollBy(0, scrollDelta)
            }
        }
    }

    private fun recordContactPickStart(event: String, target: ContactPickTarget) {
        vm.submitTrackingEvent(
            TrackBean(
                p = PageInfoBank,
                act = event,
                result = System.currentTimeMillis().toString(),
            ),
        )
        contactPickTarget = target
        pickContact()
    }

    private fun setBottomActionVisible(visible: Boolean) {
        shouldShowBottomAction = visible
        binding.submissionActionBar.isVisible = visible
    }

    private fun selectWithdrawMethod(method: WithdrawMethod) = with(binding.disbursementForm) {
        selectedWithdrawMethod = method
        disbursementMethodField.setText(getString(
            if (method == WithdrawMethod.BANK) R.string.bank else R.string.e_wallet,
        ))
        disbursementMethodField.hideError()
        bankDetailsSection.isVisible = method == WithdrawMethod.BANK
        walletDetailsSection.isVisible = method == WithdrawMethod.WALLET
    }

    private fun clearWithdrawMethodSelection() = with(binding.disbursementForm) {
        selectedWithdrawMethod = null
        shouldShowWalletPicker = false
        walletBean = null
        disbursementMethodField.setText(null)
        disbursementMethodField.hideError()
        bankDetailsSection.isVisible = false
        walletDetailsSection.isVisible = false
    }

    private fun confirmPayoutSetupExit() {
        if (shouldShowBottomAction) {
            val step =
                authConfigList.size - max(0, authConfigList.indexOf("BANK"))
            showConfirmDialog(
                desc = String.format(
                    getString(R.string.auth_exit_confirm),
                    step.toString()
                ),
                cancel = getString(R.string.give_up),
                ok = getString(R.string.continue_str),
                cancelButtonSurfaceSecondary = true,
                highLight = step.toString(),
                cancelAction = {
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoBank,
                            act = ACT_clickBack
                        )
                    )
                    finish()
                }
            ) {
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoBank,
                        act = ACT_clickContinue
                    )
                )
            }
        } else {
            finish()
        }
    }

    private fun submit() {
        vm.submitTrackingEvent(TrackBean(p = PageInfoBank, act = ACT_clickNext))
        trackEvent(SUPPLEMENTARY_INFO_COMMIT)
        val isWallet = selectedWithdrawMethod == WithdrawMethod.WALLET
        val contactEntries = arrayListOf(
            RelativesBean(
                relativesStatus,
                binding.primaryContactNameField.getText(),
                binding.primaryContactPhoneField.getText(),
            ),
            RelativesBean(
                friendStatus,
                binding.secondaryContactNameField.getText(),
                binding.secondaryContactPhoneField.getText(),
            ),
        )
        if (
            additionalContactStatus != null &&
            binding.additionalContactNameField.getText().isNotBlank() &&
            binding.additionalContactPhoneField.getText().isNotBlank()
        ) {
            contactEntries += RelativesBean(
                additionalContactStatus,
                binding.additionalContactNameField.getText(),
                binding.additionalContactPhoneField.getText(),
            )
        }
        vm.submitBankAndCtsInfo(
            ApiRequest(
                bankInfoId = if (isWallet) null else bankBean?.countryId?.toString(),
                bankId = if (isWallet) null else bankBean?.id?.toString(),
                accountUser = binding.disbursementForm.accountHolderField.getText(),
                bankNo = if (isWallet) null else binding.disbursementForm.bankAccountNumberField.getText(),
                bankCode = if (isWallet) null else bankBean?.bankCode,
                bankName = if (isWallet) null else bankBean?.bankName,
                payWay = if (isWallet) "WALLET" else "CARD",
                walletId = if (isWallet) walletBean?.id else null,
                accountCode = if (isWallet) binding.disbursementForm.walletAccountNumberField.getText().trim() else null,
                relativesInfoVOList = contactEntries,
            )
        )
    }

    private var bankBean: AccountChannelResponse? = null
    private var walletBean: AccountMethodResponse? = null
    private var relativesStatus: Int? = null
    private var friendStatus: Int? = null
    private var additionalContactStatus: Int? = null
    override fun initObserve() = with(vm) {
        super.initObserve()
        accountVm.payChannelList.observe(this@ContactPayoutActivity) {
            val channelList = it ?: arrayListOf()
            chooseBankDialog(
                channelList
            ) { bean ->
                selectWithdrawMethod(WithdrawMethod.BANK)
                binding.disbursementForm.bankSelectorField.setText(bean.bankName)
                binding.disbursementForm.bankSelectorField.hideError()
                bankBean = bean
            }
        }
        accountVm.walletList.observe(this@ContactPayoutActivity) {
            val walletItems = it ?: arrayListOf()
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
        contractResult.observe(this@ContactPayoutActivity) {
            binding.apply {
                pageContent.isVisible = true
                pageState.hide()
                setBottomActionVisible(!isCert)
                additionalContactSection.isVisible = !isCert || it.hasAdditionalContact()
                it?.let {
                    relativesStatus = it.relatives
                    friendStatus = it.otherRelatives
                    primaryRelationshipField.setText(it.relativesStr)
                    primaryContactNameField.setText(it.relativesName)
                    primaryContactPhoneField.setText(it.relativesMobile)
                    secondaryRelationshipField.setText(it.otherRelativesStr)
                    secondaryContactNameField.setText(it.otherName)
                    secondaryContactPhoneField.setText(it.otherMobile)
                    additionalContactStatus = it.thirdRelatives
                    additionalContactNameField.setText(it.thirdName)
                    additionalContactPhoneField.setText(it.thirdMobile)
                    it.thirdRelatives?.let { thirdRelationship ->
                        vm.getContactEnum { options ->
                            val relationship = options.otherRelatives?.firstOrNull {
                                it.state == thirdRelationship
                            }
                            additionalRelationshipField.setText(relationship?.info)
                        }
                    }
                    disbursementForm.accountHolderField.setText(it.accountUser)
                }
            }
        }
        submitBankAndCtsResult.observe(this@ContactPayoutActivity) {
            homeVm.getUserAuthStatus()
        }
        personalVm.personalResult.observe(this@ContactPayoutActivity) {
            binding.disbursementForm.accountHolderField.setText(it?.firstName)
        }
        homeVm.userAuthStatusResult.observe(this@ContactPayoutActivity) {
            it?.routeToNextAuthStep(this@ContactPayoutActivity)
            finish()
        }
    }

    private fun EmploymentContactResponse?.hasAdditionalContact(): Boolean {
        return this?.thirdRelatives != null ||
            !this?.thirdName.isNullOrBlank() ||
            !this?.thirdMobile.isNullOrBlank()
    }

    private fun fillWalletAccountFromLoginPhone() {
        with(binding.disbursementForm) {
            if (walletAccountNumberField.getText().isNotBlank()) return
            val phone = SessionStore.loginInfo?.phone.orEmpty()
            if (phone.isBlank()) return
            val walletAccount = if (phone.startsWith('0')) phone else "0$phone"
            walletAccountNumberField.setText(walletAccount)
            walletAccountConfirmationField.setText(walletAccount)
        }
    }

    /** Shows Gcash immediately, then resolves its server-issued ID in the background. */
    private fun selectDefaultWallet() = with(binding.disbursementForm) {
        shouldShowWalletPicker = false
        walletBean = null
        selectWithdrawMethod(WithdrawMethod.WALLET)
        walletProviderField.setText(getString(R.string.gcash))
        walletProviderField.hideError()
        fillWalletAccountFromLoginPhone()
        accountVm.getWalletList()
    }

    private fun applyWalletSelection(wallet: AccountMethodResponse) = with(binding.disbursementForm) {
        selectWithdrawMethod(WithdrawMethod.WALLET)
        walletProviderField.setText(wallet.walletName)
        walletProviderField.hideError()
        walletBean = wallet
        fillWalletAccountFromLoginPhone()
    }

    private var contactPickTarget = ContactPickTarget.PRIMARY

    @SuppressLint("Range")
    private val pickContactLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    it.getContactInfo { name, number ->
                        when (contactPickTarget) {
                            ContactPickTarget.PRIMARY -> {
                                binding.primaryContactNameField.setText(name)
                                binding.primaryContactPhoneField.setText(number)
                                vm.submitTrackingEvent(
                                    TrackBean(
                                        p = PageInfoBank,
                                        act = ACT_selectContactName1End,
                                        result = System.currentTimeMillis().toString(),
                                    ),
                                )
                            }
                            ContactPickTarget.SECONDARY -> {
                                binding.secondaryContactNameField.setText(name)
                                binding.secondaryContactPhoneField.setText(number)
                                vm.submitTrackingEvent(
                                    TrackBean(
                                        p = PageInfoBank,
                                        act = ACT_selectContactName2End,
                                        result = System.currentTimeMillis().toString(),
                                    ),
                                )
                            }
                            ContactPickTarget.ADDITIONAL -> {
                                binding.additionalContactNameField.setText(name)
                                binding.additionalContactPhoneField.setText(number)
                                vm.submitTrackingEvent(
                                    TrackBean(
                                        p = PageInfoBank,
                                        act = ACT_selectContactName3End,
                                        result = System.currentTimeMillis().toString(),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

    fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        pickContactLauncher.launch(intent)
    }
}
