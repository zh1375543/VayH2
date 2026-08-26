package com.velora.portal.journey.lending.dashboard.presentation

import android.app.Dialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.velora.portal.R
import com.velora.portal.journey.access.presentation.AuthStatusViewModel
import com.velora.portal.journey.access.presentation.routeToNextAuthStep
import com.velora.portal.platform.design.base.BaseFragment
import com.velora.portal.platform.common.data.ACT_clickClose
import com.velora.portal.platform.common.data.ACT_clickImmediate
import com.velora.portal.platform.common.data.ACT_userAppBankMyCard
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.bean.Event
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.platform.common.data.signBackHome
import com.velora.portal.databinding.FragmentDashboardHomeBinding
import com.velora.portal.journey.lending.catalog.presentation.SignatureCaptureActivity
import com.velora.portal.journey.lending.catalog.presentation.MultiLoanOfferActivity
import com.velora.portal.journey.lending.catalog.presentation.LoanDashboardViewModel
import com.velora.portal.journey.lending.catalog.presentation.LoanProductDetailActivity
import com.velora.portal.journey.lending.catalog.presentation.ProductOptionsViewModel
import com.velora.portal.domain.customer.model.VerificationProgressResponse
import com.velora.portal.journey.lending.dashboard.model.VisitorPortalResponse
import com.velora.portal.domain.credit.model.CatalogEntry
import com.velora.portal.application.PortalHostActivity
import com.velora.portal.journey.communication.support.presentation.FeedbackViewModel
import com.velora.portal.journey.lending.dashboard.presentation.adapter.LoanCatalogAdapter
import com.velora.portal.journey.lending.dashboard.presentation.state.HomeEffect
import com.velora.portal.journey.lending.dashboard.presentation.state.HomeProductUi
import com.velora.portal.journey.lending.dashboard.presentation.state.MemberHomeUiState
import com.velora.portal.journey.lending.catalog.presentation.dialog.createAvailableCreditDialog
import com.velora.portal.journey.lending.catalog.presentation.dialog.createNewProductDialog
import com.velora.portal.platform.design.extension.animateAmount
import com.velora.portal.platform.design.extension.resetScale
import com.velora.portal.platform.design.extension.setClickableTextWithScale
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.design.extension.stopScaleAnimation
import com.velora.portal.platform.design.dialog.showAppRatingDialog
import com.velora.portal.journey.lending.dashboard.presentation.dialog.showCreditUnderReviewDialog
import com.velora.portal.journey.lending.dashboard.presentation.dialog.showPreCreditExpiredDialog
import com.velora.portal.platform.common.util.LOAN_GET_NOW_CLICK
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.platform.common.util.ExternalActionLauncher
import com.velora.portal.platform.common.util.countdownTimer
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix
import com.velora.portal.platform.common.util.platform.formatLoanTerm
import com.velora.portal.platform.common.util.platform.requireLogin
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.text.toJsonString
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.journey.account.accounts.presentation.PayoutAccountListActivity
import kotlinx.coroutines.Job

class HomeFragment : BaseFragment<FragmentDashboardHomeBinding>(R.layout.fragment_dashboard_home) {

    override val binding by viewBinding(FragmentDashboardHomeBinding::bind)

    private val vm by viewModels<HomeViewModel>()
    private val loanDashboardVm by viewModels<LoanDashboardViewModel>()
    private val authStatusVm by viewModels<AuthStatusViewModel>()
    private val feedbackVm by viewModels<FeedbackViewModel>()
    private val productVm by viewModels<ProductOptionsViewModel>()

    private val homeAdapter by lazy {
        LoanCatalogAdapter().apply {
            setOnChildClickListener { view, _, position ->
                when (view.id) {
                    R.id.btnOfferAction -> {
                        trackEvent(LOAN_GET_NOW_CLICK)
                        val item = items[position]
                        if (!item.canApply) return@setOnChildClickListener
                        val product = item.product
                        if (product.creditStatus == 2) {
                            context.showPreCreditExpiredDialog(product.enableLoanStr ?: "")
                            return@setOnChildClickListener
                        }
                        if (product.creditStatus == 0) {
                            context.showCreditUnderReviewDialog()
                            return@setOnChildClickListener
                        }
                        when (product.jumpType) {
                            1 -> product.downloadUrl?.let {
                                ExternalActionLauncher.openBrowser(requireContext(), it)
                            }
                            2 -> ExternalActionLauncher.openStoreListing(
                                requireContext(),
                                product.downloadUrl,
                            )
                            4 -> context.start<MultiLoanOfferActivity>()
                            else -> {
                                productVm.getProductDetail(
                                    PageHome,
                                    product.productId.toString(),
                                    product.maxLoanAmount.toString(),
                                    true
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private var newProductDialog: Dialog? = null
    private var creditDialog: Dialog? = null
    private var hasShownCreditDialog = false

    override fun initView() {
        setupProductList()
        setupPayoutAccountAction()
        setupApplicationActions()
        setupRefreshTriggers()
    }

    private fun setupProductList() {
        binding.contentLayout.rvOffers.adapter = homeAdapter
    }

    private fun setupPayoutAccountAction() = with(binding.contentLayout) {
        btnManagePayoutAccount.singleClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageHome,
                    act = ACT_userAppBankMyCard
                )
            )
            it.context.start<PayoutAccountListActivity>()
        }
    }

    private fun setupApplicationActions() = with(binding.contentLayout) {
        btnStartApplication.singleClick {
            it.context.requireLogin {
                isGoAuth = true
                authStatusVm.getUserAuthStatus()
            }
        }
        btnOpenOffers.singleClick {
            it.context.requireLogin {
                context?.start<MultiLoanOfferActivity>()
            }
        }
        btnDismissAccountAlert.singleClick {
            payoutAccountAlert.isVisible = false
        }
    }

    private fun setupRefreshTriggers() = with(binding) {
        contentLayout.btnReloadDashboard.singleClick {
            refreshData()
        }
        pageState.setOnRetryClickListener {
            refreshData()
        }
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private var isGoAuth = false
    private var hasRenderedLoadError = false

    override fun onResume() {
        super.onResume()
        // ViewPager keeps HomeFragment alive, so allow the credit dialog once per Home entry.
        hasShownCreditDialog = false
        refreshData()
    }

    fun refreshData() = with(binding) {
        hasRenderedLoadError = false
        pageContent.isVisible = false
        pageState.showLoading()
        contentLayout.apply {
            topLayout.isVisible = true
            contentLayout.isVisible = true
        }
        calmLayout.calmLayout.isVisible = false
        if (SessionStore.isLoggedIn) {
            isGoAuth = false
            authStatusVm.getUserAuthStatus()
        } else {
            vm.getUnAuthData()
        }
        vm.getBannerList()
    }

    private var timeJob: Job? = null
    private var loanDateStr: String? = null

    override fun initObserve() {
        vm.loadFailedResult.observe(this@HomeFragment) {
            renderLoadError()
        }
        loanDashboardVm.loadFailedResult.observe(this@HomeFragment) {
            renderLoadError()
        }
        authStatusVm.loadFailedResult.observe(this@HomeFragment) {
            renderLoadError()
        }
        authStatusVm.userAuthStatusResult.observe(this@HomeFragment) {
            handleUserAuthStatus(it)
        }
        vm.guestResult.observe(this@HomeFragment) {
            it?.let(::handleUnAuthData)
        }
        loanDashboardVm.memberHomeState.observe(this@HomeFragment, ::renderMemberHome)
        loanDashboardVm.homeEffect.observe(this@HomeFragment, ::handleHomeEffectEvent)
        vm.bannerResult.observe(this@HomeFragment) {
            binding.contentLayout.campaignBanner.setData(it ?: emptyList())
            binding.contentLayout.campaignBanner.isVisible = !it.isNullOrEmpty()
        }
        productVm.detailResult.observe(this@HomeFragment) {
            handleProductDetail(it)
        }
    }

    private fun renderLoadError() {
        if (hasRenderedLoadError) return

        hasRenderedLoadError = true
        binding.swipeRefreshLayout.isRefreshing = false
        binding.pageContent.isVisible = false
        binding.pageState.showError()
    }

    private fun handleUserAuthStatus(data: VerificationProgressResponse?) {
        if (isGoAuth) {
            data?.routeToNextAuthStep(binding.root.context, false)
            return
        }
        isGoAuth = false
        authStatusVm.fetchAuthConfigList { configList ->
            if (data?.isPass(configList) == true) {
                loanDashboardVm.getMemberHomeData()
            } else {
                vm.getUnAuthData()
            }
        }
    }

    private fun handleUnAuthData(data: VisitorPortalResponse) = with(binding) {
        pageContent.isVisible = true
        pageState.hide()
        swipeRefreshLayout.isRefreshing = false
        contentLayout.apply {
            tvAmount.animateAmount(data.maxAmount, prefix = data.currencySymbol ?: "")
            tvLoanAmount.text = getString(R.string.l_amount)
            tvPercent.text = data.annualizedInterestRate
            tvPeriod.text = root.context.formatLoanTerm(data.loanTerm)
            tvRateLabel.text = data.recommendText
            tvRateLabel.isVisible = !data.recommendText.isNullOrEmpty()
            memberCreditPanel.isVisible = false
            guestCreditPanel.isVisible = true
            authActionLayout.isVisible = false
            unAuthActionLayout.isVisible = true
            reviewActionLayout.isVisible = false
            homeTicketCard.isVisible = true
            topLayout.isVisible = true
            questionLayout.isVisible = true
            offerSection.isVisible = false
            emptyProduct.isVisible = false
            payoutAccountAlert.isVisible = false
            calmLayout.calmLayout.isVisible = false
            reviewStatusPanel.isVisible = false
            rejectionStatusPanel.isVisible = false
            tvQuick.isVisible = true
            marqueeView.setTexts(isWhiteColor = false)
        }
    }

    private fun renderMemberHome(state: MemberHomeUiState) = with(binding) {
        pageContent.isVisible = true
        pageState.hide()
        swipeRefreshLayout.isRefreshing = false
        contentLayout.apply {
            tvAmount2.animateAmount(
                state.availableAmount,
                prefix = state.creditCurrencySymbol ?: ""
            )
            tvMaxAmount.text = state.totalAmount.formatAmountWithPrefix(state.creditCurrencySymbol)
            tvUsedAmount.text = state.usedAmount.formatAmountWithPrefix(state.creditCurrencySymbol)
            tvLoanRateLabel.text = state.recommendText
            tvLoanRateLabel.isVisible = !state.recommendText.isNullOrEmpty()
            guestCreditPanel.isVisible = false
            memberCreditPanel.isVisible = state.showAuthenticatedLayout
            unAuthActionLayout.isVisible = false
            authActionLayout.isVisible = state.showAuthenticatedLayout
            tvQuick.isVisible = true
            questionLayout.isVisible = false
            val showReviewCard = state.showReviewLayout && !state.showCalmPage
            val showRejectedCard = state.showRejectedLayout && !state.showCalmPage
            val showProductChrome = !state.showCalmPage
            val showMainCreditCard =
                state.showCreditHeader && showProductChrome
            val showHomeHeader =
                showMainCreditCard || showReviewCard || showRejectedCard

            calmLayout.calmLayout.isVisible = state.showCalmPage
            topLayout.isVisible = showHomeHeader
            homeTicketCard.isVisible = showHomeHeader
            reviewStatusPanel.isVisible = showReviewCard
            rejectionStatusPanel.isVisible = showRejectedCard
            reviewActionLayout.isVisible = showReviewCard
            if (homeTicketCard.isVisible) {
                marqueeView.setTexts()
            } else {
                marqueeView.stop()
            }
            contentLayout.isVisible = !state.showCalmPage
            loanDateStr = state.enableLoanDate
            btnOpenOffers.isEnabled = state.loanEnabled
            if (btnOpenOffers.isEnabled) btnOpenOffers.resetScale() else btnOpenOffers.stopScaleAnimation()

            payoutAccountAlert.isVisible = state.showBankError

            timeJob?.cancel()
            tvPreTimes.isVisible = state.showReviewLayout
            if (state.showReviewLayout) {
                startCountdown(60)
            }

            homeAdapter.submitItems(state.products)
            // The credit-review card is supplemental: product offers still need to be
            // available while the user's credit decision is pending or rejected.
            offerSection.isVisible = state.showProductList && showProductChrome
            emptyProduct.isVisible = state.showEmptyProducts && showProductChrome

            tvPreTips.text =
                String.format(getString(R.string.home_pre_tips), state.enableLoanDate ?: "-")
            val calmTips =
                String.format(getString(R.string.home_calm_tips3), state.enableLoanDate ?: "-")
            binding.calmLayout.tvCalmTips3.setClickableTextWithScale(
                calmTips,
                state.enableLoanDate ?: "-",
                binding.root.context.resolveColorCompat(R.color.text_body)
            )
            if (!state.showAuthenticatedLayout) {
                return@apply
            }

            if (state.showCalmPage) {
                contentLayout.isVisible = false
                return@apply
            }

            if (state.showEmptyProducts) {
                offerSection.isVisible = false
                campaignBanner.isVisible = false
                binding.calmLayout.calmLayout.isVisible = false
                topLayout.isVisible = false
            }

        }
    }

    private fun handleHomeEffectEvent(event: Event<HomeEffect>) {
        when (val effect = event.getContentIfNotHandled() ?: return) {
            HomeEffect.ShowAppRating -> {
                activity?.showAppRatingDialog { content ->
                    feedbackVm.submitFeed(content) {
                        getString(R.string.feedback_success).showToastMessage()
                    }
                }
            }

            HomeEffect.NavigateToOrders -> (activity as PortalHostActivity?)?.selectPage(1)
            is HomeEffect.ShowNewProducts -> showNewProductDialogIfNeeded(effect.products)
            is HomeEffect.ShowAvailableCredit -> showCreditDialogIfNeeded(effect)
        }
    }

    private fun showCreditDialogIfNeeded(effect: HomeEffect.ShowAvailableCredit) {
        with(binding.contentLayout) {
            if (hasShownCreditDialog) return
            if (creditDialog?.isShowing == true || newProductDialog?.isShowing == true) return

            val amount = effect.amount.formatAmountWithPrefix(effect.currencySymbol)
            hasShownCreditDialog = true
            creditDialog = root.context.createAvailableCreditDialog(amount) {
                if (!btnOpenOffers.isEnabled) return@createAvailableCreditDialog
                root.context.start<MultiLoanOfferActivity>()
            }
            creditDialog?.setOnDismissListener {
                creditDialog = null
            }
            creditDialog?.show()
        }
    }

    private fun showNewProductDialogIfNeeded(newProducts: List<HomeProductUi>) {
        if (newProducts.isEmpty()) return
        if (creditDialog?.isShowing == true || newProductDialog?.isShowing == true) return

        val dialogProducts = newProducts.map { productUi ->
            productUi.product.copy(canApply = productUi.canApply)
        }
        newProductDialog = context?.createNewProductDialog(dialogProducts, closeAction = {
            vm.submitTrackingEvent(TrackBean(p = PageHome, act = ACT_clickClose))
        }) {
            if (!binding.contentLayout.btnOpenOffers.isEnabled) return@createNewProductDialog
            vm.submitTrackingEvent(TrackBean(p = PageHome, act = ACT_clickImmediate))
            context?.start<MultiLoanOfferActivity>()
        }
        newProductDialog?.show()
    }

    @Suppress("SameParameterValue")
    private fun startCountdown(t: Long = 60L) = with(binding.contentLayout) {
        tvPreTimes.isVisible = true
        updatePreCountdown(t)
        timeJob = lifecycleScope.countdownTimer(
            t, {
                updatePreCountdown(t)
            }, end = {
                tvPreTimes.isVisible = false
            }
        ) {
            updatePreCountdown(it)
        }
    }

    private fun updatePreCountdown(seconds: Long) = with(binding.contentLayout) {
        val fullText = String.format(getString(R.string.home_refuse_times), seconds)
        tvPreTimes.setClickableTextWithScale(
            fullText,
            seconds.toString(),
            root.context.resolveColorCompat(R.color.surface_primary)
        )
    }

    private fun handleProductDetail(data: CatalogEntry?) {
        if (data == null) return
        if (signBackHome) {
            val map: MutableMap<Long?, Int?> = HashMap()
            data.productInstallmentPlanDTOList?.let { list ->
                val index = list.indexOfFirst { it.isDefault == 1 }.coerceAtLeast(0)
                if (index < list.size) {
                    map[list[index].productId] = list[index].planNums
                }
            }
            val termMap: MutableMap<Long?, Long?> = HashMap()
            data.loanTermConfigDTOList?.let { list ->
                val index = list.indexOfFirst { it.defaultSign == 1 }.coerceAtLeast(0)
                if (index < list.size) {
                    termMap[data.id] = list[index].id
                }
            }
            SignatureCaptureActivity.Companion.launch(
                binding.root.context,
                data.bankInfoId,
                null,
                data.id.toString(),
                data.bankInfoId,
                data.maxLoanAmount?.toString(),
                if (map.isEmpty()) null else map.toJsonString(),
                termMap.toJsonString(),
                true
            )
        } else {
            context?.start<LoanProductDetailActivity> {
                putExtra("product", data)
            }
        }
    }
}
