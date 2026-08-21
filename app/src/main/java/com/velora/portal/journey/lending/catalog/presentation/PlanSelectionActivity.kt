package com.velora.portal.journey.lending.catalog.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.ACT_clickApply
import com.velora.portal.platform.common.data.ACT_clickConfirm
import com.velora.portal.platform.common.data.ACT_in
import com.velora.portal.platform.common.data.ACT_userAppBankMyCard
import com.velora.portal.platform.common.data.PageProductDetail
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.domain.payout.model.LinkedAccountResponse
import com.velora.portal.journey.lending.catalog.presentation.adapter.ComboAdapter
import com.velora.portal.journey.account.accounts.presentation.dialog.chooseAccountsDialog
import com.velora.portal.platform.design.extension.resetScale
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.journey.lending.catalog.presentation.dialog.showLoanAgreementDialog
import com.velora.portal.platform.common.util.loanevent.LoanEvent
import com.velora.portal.platform.common.util.loanevent.LoanEventRecorder
import com.velora.portal.platform.common.util.ORDER_COMMIT
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.PermissionScenario
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix
import com.velora.portal.platform.common.util.maskSensitive
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.platform.common.util.getPayoutAccountTypeLabel
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.databinding.ActivityOfferBundleBinding
import com.velora.portal.journey.account.accounts.presentation.LinkedAccountViewModel

class PlanSelectionActivity : BaseActivity<ActivityOfferBundleBinding>() {

    override val binding by viewBinding(ActivityOfferBundleBinding::inflate)

    private val togetherAdapter by lazy { ComboAdapter() }
    private val vm by viewModels<ApplicationProcessViewModel>()
    private val accountVm by viewModels<LinkedAccountViewModel>()

    private var cardInfo: LinkedAccountResponse? = null
    private var hasRecordedEnterEvent = false

    override fun initView() {
        prepareBundleScreen()
        connectAccountSwitcher()
        connectRetryHandler()
        connectBundleApplication()
    }

    private fun prepareBundleScreen() = with(binding) {
        LoanEventRecorder.setEventFileSuffix((SessionStore.loginInfo?.id ?: 111).toString())

        titleBar.setNavigationAction { finish() }
        registerTrackedBackHandler(vm) { finish() }
        rvBundleList.adapter = togetherAdapter
    }

    private fun connectAccountSwitcher() = with(binding) {
        btnSwitchAccount.singleClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_userAppBankMyCard,
                ),
            )
            LoanEventRecorder.record(LoanEvent.CLICK_CHOOSE_WALLET)
            accountVm.getLoanAccountList { }
        }
    }

    private fun connectRetryHandler() = with(binding) {
        pageState.setOnRetryClickListener {
            vm.getTogetherLoan()
        }
    }

    private fun connectBundleApplication() = with(binding) {
        btnWithdraw.resetScale()
        btnWithdraw.singleClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_clickApply,
                    result = productIdsForTrack() + "|" + System.currentTimeMillis(),
                ),
            )
            LoanEventRecorder.record(LoanEvent.CLICK_APPLY_LOAN)
            PermissionCoordinator.request(this@PlanSelectionActivity, PermissionScenario.DEVICE_RISK) {
                val (productInstallmentMap, termIdMap) = buildSubmissionMaps()
                trackEvent(ORDER_COMMIT)
                showLoanAgreementDialog(isTogether = true) {
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageProductDetail,
                            act = ACT_clickConfirm,
                        ),
                    )
                    LoanEventRecorder.record(LoanEvent.CLICK_SUBMIT_LOAN)
                    RequestStatusActivity.Companion.launch(
                        this@PlanSelectionActivity,
                        ArrayList(togetherAdapter.items),
                        null,
                        cardInfo?.id ?: 0L,
                        null,
                        null,
                        if (productInstallmentMap.isEmpty()) {
                            null
                        } else {
                            productInstallmentMap.toJsonString()
                        },
                        if (termIdMap.isEmpty()) {
                            null
                        } else {
                            termIdMap.toJsonString()
                        },
                        payWay = cardInfo?.payWay.orEmpty(),
                    )
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LoanEventRecorder.initializeBaseServerTime(System.currentTimeMillis())
        LoanEventRecorder.record(LoanEvent.VIEW_ENTER_LOAN)
        vm.getTogetherLoan()
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        togetherLoanState.observe(this@PlanSelectionActivity) { state ->
            render(state)
        }
        observeAccounts()
    }

    private fun render(state: PageLoadState<com.velora.portal.domain.credit.model.MemberOverviewResponse>) {
        binding.contentLayout.isVisible = state is PageLoadState.Content
        binding.bottomUiGroup.isVisible = state is PageLoadState.Content
        when (state) {
            PageLoadState.Loading -> binding.pageState.showLoading()
            PageLoadState.Error -> binding.pageState.showError()
            PageLoadState.Empty -> Unit
            is PageLoadState.Content -> renderLoanOffer(state.data)
        }
    }

    private fun renderLoanOffer(loan: com.velora.portal.domain.credit.model.MemberOverviewResponse) {

            val products = loan.showProducts.orEmpty().onEach { product ->
                product.canApply = true
                product.isTogether = true
            }
            togetherAdapter.submitItemsWithState(products)

            if (!hasRecordedEnterEvent) {
                hasRecordedEnterEvent = true
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageProductDetail,
                        act = ACT_in,
                        result = productIdsForTrack() + "|" + System.currentTimeMillis(),
                    ),
                )
            }

                 renderPayoutAccount(
                LinkedAccountResponse(
                    id = loan.bankInfoId ?: loan.userCashWalletId,
                    bankNo = loan.bankNo ?: loan.walletAccount,
                    payWay = if (loan.bankInfoId != null) "CARD" else "WALLET",
                ),
            )
            binding.tvTotalAmount.text = loan.canApplyAmount.formatAmountWithPrefix(loan.currencySymbol)
            binding.tvBundleCount.text = products.size.toString()
            binding.pageState.hide()
    }

    private fun observeAccounts() {
        accountVm.loanAccountList.observe(this@PlanSelectionActivity) { accounts ->
            accounts ?: return@observe
            chooseAccountsDialog(
                cardNo = cardInfo?.bankNo,
                list = accounts,
                selectedAccountId = cardInfo?.id,
                selectedPayWay = cardInfo?.payWay,
            ) { card ->
                renderPayoutAccount(card)
            }
        }
    }

    private fun renderPayoutAccount(account: LinkedAccountResponse) = with(binding) {
        cardInfo = account
        tvPayWay.text = getPayoutAccountTypeLabel(account.payWay)
        tvAccountNo.text = (account.account ?: account.bankNo).maskSensitive().orEmpty()
        walletTipPanel.isVisible = account.payWay == "WALLET"
    }

    override fun onStop() {
        super.onStop()
        LoanEventRecorder.record(LoanEvent.VIEW_QUIT_LOAN)
        LoanEventRecorder.flush()
    }

    private fun productIdsForTrack(): String = togetherAdapter.items.joinToString(",") { product ->
        (product.id ?: product.productId).toString()
    }

    private fun buildSubmissionMaps(): Pair<MutableMap<Long?, Int?>, MutableMap<Long?, Long?>> {
        val productInstallmentMap = mutableMapOf<Long?, Int?>()
        val termIdMap = mutableMapOf<Long?, Long?>()

        togetherAdapter.items.forEach { product ->
            val plans = product.loanTermConfigDTOList
            if (plans.isNullOrEmpty()) return@forEach

            val selectedIndex = (product.selectedTermIndex ?: 0).coerceIn(plans.indices)
            val selectedPlan = plans[selectedIndex]
            val productId = product.id ?: product.productId

            termIdMap[productId] = selectedPlan.id
            productInstallmentMap[productId] =
                selectedPlan.productInstallmentPlanDTOList?.firstOrNull()?.planNums
        }

        return productInstallmentMap to termIdMap
    }
}
