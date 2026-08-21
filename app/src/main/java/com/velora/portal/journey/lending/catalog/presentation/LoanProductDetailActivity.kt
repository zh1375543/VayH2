package com.velora.portal.journey.lending.catalog.presentation

import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.ACT_clickApply
import com.velora.portal.platform.common.data.ACT_clickConfirm
import com.velora.portal.platform.common.data.ACT_in
import com.velora.portal.platform.common.data.ACT_userAppBankMyCard
import com.velora.portal.platform.common.data.PageProductDetail
import com.velora.portal.platform.common.data.AGREEMENT_ABOUT
import com.velora.portal.platform.common.data.PRODUCT_AGREEMENT
import com.velora.portal.platform.common.data.bean.ClickablePart
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.databinding.ScreenLoanProductDetailBinding
import com.velora.portal.domain.credit.model.CatalogItemBean
import com.velora.portal.domain.payout.model.LinkedAccountResponse
import com.velora.portal.platform.browser.presentation.ContentBrowserActivity
import com.velora.portal.journey.account.accounts.presentation.dialog.chooseAccountsDialog
import com.velora.portal.platform.design.extension.resetScale
import com.velora.portal.platform.design.extension.setSpannableClickableTexts
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.journey.account.accounts.presentation.dialog.showBankCardErrorDialog
import com.velora.portal.journey.lending.catalog.presentation.dialog.showLoanAgreementDialog
import com.velora.portal.platform.common.util.loanevent.LoanEvent
import com.velora.portal.platform.common.util.loanevent.LoanEventRecorder
import com.velora.portal.platform.common.util.LogUtil
import com.velora.portal.platform.common.util.ORDER_COMMIT
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.PermissionScenario
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix
import com.velora.portal.platform.common.util.maskSensitive
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.platform.common.util.getPayoutAccountTypeLabel
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.journey.account.accounts.presentation.PayoutAccountListActivity
import com.velora.portal.journey.account.accounts.presentation.LinkedAccountViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class LoanProductDetailActivity : BaseActivity<ScreenLoanProductDetailBinding>() {

    override val binding by viewBinding(ScreenLoanProductDetailBinding::inflate)
    private val vm by viewModels<ProductOptionsViewModel>()
    private val accountVm by viewModels<LinkedAccountViewModel>()

    private val product by lazy { intent.getParcelableExtra<CatalogItemBean>("product") }
    private var cardInfo: LinkedAccountResponse? = null

    private lateinit var leaseUrl: String
    private lateinit var pawnUrl: String
    private var isAddCard = false

    override fun initView() {
        prepareProductExperience()
        connectProductExploration()
        connectRepaymentPlan()
        connectLoanApplication()
    }

    private fun prepareProductExperience() = with(binding) {
        vm.submitTrackingEvent(
            TrackBean(
                p = PageProductDetail,
                act = ACT_in,
                result = product?.id.toString() + "|" + System.currentTimeMillis()
            )
        )
        LoanEventRecorder.setEventFileSuffix((SessionStore.loginInfo?.id ?: 111).toString())
        titleBar.setNavigationAction { exitOfferFlow() }
        registerTrackedBackHandler(vm) {
            exitOfferFlow()
        }
        pageState.setOnRetryClickListener {
            vm.getProductDetail(
                PageProductDetail,
                product?.id.toString(), product?.maxLoanAmount.toString()
            ) {}
        }
    }

    private fun connectProductExploration() = with(binding) {
        tvPrivacy.setSpannableClickableTexts(
            String.format(
                getString(R.string.product_detail_agreement),
                getString(R.string.lease_contract)
            ), arrayListOf(
                ClickablePart(
                    getString(R.string.lease_contract), resolveColorCompat(R.color.brand_primary), onClick = {
                        LoanEventRecorder.record(LoanEvent.CLICK_OPEN_AGREEMENT)
                        ContentBrowserActivity.Companion.launch(
                            this@LoanProductDetailActivity, getString(R.string.lease_contract), leaseUrl
                        )
                    }),
            )
        )
        btnSwitchAccount.singleClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_userAppBankMyCard
                )
            )
            LoanEventRecorder.record(LoanEvent.CLICK_CHOOSE_WALLET)
            accountVm.getLoanAccountList {}
        }
        tvLeaseInfo.singleClick {
            ContentBrowserActivity.Companion.launch(
                this@LoanProductDetailActivity, tvLeaseInfo.text.toString(), AGREEMENT_ABOUT
            )
        }
        productDetailsCard.isVisible = true
    }

    private fun connectRepaymentPlan() = with(binding) {
        repaymentPlanView.apply {
            onTermChanged = { productId, termId ->
                termIdMap.clear()
                termIdMap[productId] = termId
            }

            onInstallmentChanged = { productId, planNum ->
                productInstallmentMap.clear()
                productInstallmentMap[productId] = planNum
            }

            onPlanSelected = { selectedPlan ->
                renderOfferSummary(selectedPlan)
            }
        }
    }

    private fun connectLoanApplication() = with(binding) {
        btnWithdraw.resetScale()
        btnWithdraw.singleClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageProductDetail,
                    act = ACT_clickApply,
                    result = product?.id.toString() + "|" + System.currentTimeMillis()
                )
            )
            LoanEventRecorder.record(LoanEvent.CLICK_APPLY_LOAN)
            PermissionCoordinator.request(this@LoanProductDetailActivity, PermissionScenario.DEVICE_RISK) {
                trackEvent(ORDER_COMMIT)
                showLoanAgreementDialog(
                    productId = vm.detailResult.value?.id?.toString(),
                    amount = vm.detailResult.value?.loanAmount?.toString()
                ) {
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageProductDetail,
                            act = ACT_clickConfirm,
                        )
                    )
                    LoanEventRecorder.record(LoanEvent.CLICK_SUBMIT_LOAN)
                    if (product?.isSign == 0) {
                        SignatureCaptureActivity.Companion.launch(
                            this@LoanProductDetailActivity,
                            cardInfo?.id,
                            null,
                            product?.id.toString(),
                            cardInfo?.id,
                            product?.loanAmount?.toString(),
                            if (productInstallmentMap.isEmpty()) null else productInstallmentMap.toJsonString(),
                            if (termIdMap.isEmpty()) null else termIdMap.toJsonString(),
                            payWay = cardInfo?.payWay ?: "CARD",
                        )
                    } else {
                        LoanSubmissionResultActivity.Companion.launch(
                            this@LoanProductDetailActivity,
                            null,
                            product?.id.toString(),
                            cardInfo?.id,
                            null,
                            product?.loanAmount?.toString(),
                            if (productInstallmentMap.isEmpty()) null else productInstallmentMap.toJsonString(),
                            if (termIdMap.isEmpty()) null else termIdMap.toJsonString(),
                            payWay = cardInfo?.payWay ?: "CARD",
                        )
                        finish()
                    }
                }
            }
        }
    }

    private fun exitOfferFlow() {
        finish()
    }

    private var isFirstEnter = true

    // Field used to persist the selected term across resume.
    private var savedTermIndex: Int = -1
    override fun onResume() {
        super.onResume()
        LoanEventRecorder.initializeBaseServerTime(System.currentTimeMillis())
        LoanEventRecorder.record(LoanEvent.VIEW_ENTER_LOAN)
        if (product != null && isFirstEnter) {
            product?.let {
                vm.showProductDetail(it)
            }
            isFirstEnter = false
        } else {
            // save current selection state before refreshing
            vm.detailResult.value?.let { old ->
                savedTermIndex = old.selectedTermIndex ?: -1
            }
            vm.getProductDetail(
                PageProductDetail,
                product?.id.toString(), product?.loanAmount.toString()
            ) {}
        }
    }

    override fun onStop() {
        super.onStop()
        LoanEventRecorder.record(LoanEvent.VIEW_QUIT_LOAN)
        LoanEventRecorder.flush()
    }

    private var isShowBankcardError = false
    private val productInstallmentMap: MutableMap<Long?, Int?> = HashMap()
    private val termIdMap: MutableMap<Long?, Long?> = HashMap()
    override fun initObserve() = with(vm) {
        super.initObserve()
        productDetailState.observe(this@LoanProductDetailActivity) { state ->
            render(state)
        }
        accountVm.loanAccountList.observe(this@LoanProductDetailActivity) {
            it?.let {
                chooseAccountsDialog(
                    cardNo = cardInfo?.bankNo,
                    list = it,
                    selectedAccountId = cardInfo?.id,
                    selectedPayWay = cardInfo?.payWay,
                ) { card ->
                    renderPayoutAccount(card)
                }
            }
        }
    }

    private fun render(state: PageLoadState<CatalogItemBean>) = with(binding) {
        contentScroll.isVisible = state is PageLoadState.Content
        bottomUiGroup.isVisible = state is PageLoadState.Content
        when (state) {
            PageLoadState.Loading -> pageState.showLoading()
            PageLoadState.Error -> {
                pageState.showError()
            }
            PageLoadState.Empty -> Unit
            is PageLoadState.Content -> renderProductDetail(state.data)
        }
    }

    private fun renderProductDetail(productDetail: CatalogItemBean) = with(binding) {
                    leaseUrl =
                        PRODUCT_AGREEMENT + "userId=${SessionStore.loginInfo?.id}&productId=${productDetail.id}&amount=${productDetail.loanAmount}"
                    pawnUrl =
                        PRODUCT_AGREEMENT + "userId=${SessionStore.loginInfo?.id}&productId=${productDetail.id}&amount=${productDetail.loanAmount}"
                    tvAmount.text = productDetail.loanAmount.formatAmountWithPrefix(productDetail.currencySymbol)
                    renderOfferSummary(productDetail)
                    restoreOfferSelection(productDetail)

                    // if state was never saved (savedTermIndex < 0),
                    // ignore the Bean value (may be GSON default 0) and find the item with defaultSign == 1
                    if (productDetail.selectedTermIndex == null || savedTermIndex < 0) {
                        val defaultSignIndex =
                            productDetail.loanTermConfigDTOList?.indexOfFirst { it1 -> it1.defaultSign == 1 }
                                ?: -1
                        val isDefaultIndex =
                            productDetail.loanTermConfigDTOList?.indexOfFirst { it1 -> it1.isDefault == 1 }
                                ?: -1
                        productDetail.selectedTermIndex = when {
                            defaultSignIndex >= 0 -> defaultSignIndex
                            isDefaultIndex >= 0 -> isDefaultIndex
                            else -> 0
                        }
                    }
                    LogUtil.e("selectIndex1:${productDetail.selectedTermIndex}")

                    productDetailsCard.isVisible = true
                    repaymentPlanCard.isVisible = !productDetail.loanTermConfigDTOList.isNullOrEmpty()
                    repaymentPlanView.setData(productDetail)
                    renderPayoutAccount(
                        LinkedAccountResponse(
                            id = productDetail.bankInfoId ?: productDetail.userCashWalletId,
                            bankNo = productDetail.bankNo ?: productDetail.walletAccount,
                            payWay = if (productDetail.bankInfoId != null) "CARD" else "WALLET",
                        ),
                    )
                    if (productDetail.bankInfoPayOutFailSign && !isShowBankcardError) {
                        lifecycleScope.launch {
                            delay(500.milliseconds)
                            isShowBankcardError = true
                            showBankCardErrorDialog(
                                desc = getString(R.string.card_error_tips),
                                cancel = getString(R.string.already_edited),
                                ok = getString(R.string.revise)
                            ) {
                                start<PayoutAccountListActivity>()
                                isAddCard = true
                            }
                        }
                    }
        pageState.hide()
    }

    private fun renderPayoutAccount(account: LinkedAccountResponse) = with(binding) {
        cardInfo = account
        tvAccountType.text = getPayoutAccountTypeLabel(account.payWay)
        tvCard.text = (account.account ?: account.bankNo).maskSensitive().orEmpty()
        walletTipPanel.isVisible = account.payWay == "WALLET"
    }

    fun scrollToOfferActions() {
        binding.contentScroll.postDelayed({
            binding.contentScroll.fullScroll(View.FOCUS_DOWN)
        }, 200)
    }

    private fun restoreOfferSelection(newProduct: CatalogItemBean) {
        // savedTermIndex == -1 means first load, no merge needed
        if (savedTermIndex < 0) return

        newProduct.selectedTermIndex = savedTermIndex
    }

    private fun renderOfferSummary(plan: CatalogItemBean) = with(binding) {
        val currencySymbol = plan.currencySymbol ?: product?.currencySymbol
        productDetailsView.bind(plan, currencySymbol)
    }
}
