package com.velora.portal.feature.records.presentation

import android.os.Build
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.core.common.data.bean.ClickablePart
import com.velora.portal.core.common.data.bean.TrackBean
import com.velora.portal.databinding.ActivityRecordDetailBinding
import com.velora.portal.core.common.data.ACT_inOrdersDetail
import com.velora.portal.core.common.data.ACT_inRepaymentLink
import com.velora.portal.core.common.data.ORDER_STATUS_AUTO
import com.velora.portal.core.common.data.ORDER_STATUS_AUTO_FAIL
import com.velora.portal.core.common.data.ORDER_STATUS_BAD_DEBTS
import com.velora.portal.core.common.data.ORDER_STATUS_CASH
import com.velora.portal.core.common.data.ORDER_STATUS_CLOSE
import com.velora.portal.core.common.data.ORDER_STATUS_INVALID
import com.velora.portal.core.common.data.ORDER_STATUS_IN_RENEWAL
import com.velora.portal.core.common.data.ORDER_STATUS_IN_RENEWAL_PROCESS
import com.velora.portal.core.common.data.ORDER_STATUS_MANUAL
import com.velora.portal.core.common.data.ORDER_STATUS_MANUAL_FAIL
import com.velora.portal.core.common.data.ORDER_STATUS_OVERDUE
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_FAIL
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_ING
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_PENDING
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_PROCESS
import com.velora.portal.core.common.data.ORDER_STATUS_REVIEW
import com.velora.portal.core.common.data.ORDER_STATUS_SETTLE
import com.velora.portal.core.common.data.ORDER_STATUS_SETTLE_REDUCE
import com.velora.portal.core.common.data.ORDER_STATUS_SETTLE_REDUCE_OR_RENEWAL
import com.velora.portal.core.common.data.ORDER_STATUS_SETTLE_RENEWAL
import com.velora.portal.core.common.data.ORDER_STATUS_SUCCESS
import com.velora.portal.core.common.data.PRODUCT_AGREEMENT
import com.velora.portal.core.common.data.PageOrderDetail
import com.velora.portal.core.common.data.PageRepaymentLink
import com.velora.portal.feature.records.model.RecordDetailResponse
import com.velora.portal.feature.records.presentation.adapter.BorrowingFeeBreakdownAdapter
import com.velora.portal.feature.records.presentation.adapter.BorrowingScheduleAdapter
import com.velora.portal.core.common.util.LOAN_ORDER_CONFIRMATION_PAGE
import com.velora.portal.core.common.util.PageLoadState
import com.velora.portal.core.common.util.context.resolveColorCompat
import com.velora.portal.core.common.util.showToastMessage
import com.velora.portal.core.ui.extension.setSpannableClickableTexts
import com.velora.portal.core.ui.extension.setRoundedRectangleBackground
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.trackEvent
import com.velora.portal.feature.content.presentation.ContentBrowserActivity
import com.velora.portal.core.ui.component.StatefulActionButton
import com.velora.portal.feature.checkout.presentation.dialog.showRepayAndReapplyDialog
import com.velora.portal.core.common.util.text.formatAmountWithPrefix
import com.velora.portal.core.common.util.isPositive
import com.velora.portal.core.common.util.viewBinding
import java.math.BigDecimal
import kotlin.toString

class RecordDetailActivity :
    BaseActivity<ActivityRecordDetailBinding>() {

    override val binding by viewBinding(ActivityRecordDetailBinding::inflate)
    private val isFromBatch by lazy { intent.getBooleanExtra("isFromBatch", false) }
    private val vm by viewModels<RecordCenterViewModel>()

    private val orderId by lazy { intent.getLongExtra("orderId", 0L) }
    // Server state reloanButtonSign supports "0"-"4"; null/unknown values fall back to state 2.
    private var currentButtonSign: String? = null
    private var isCurrentOrderDue = false
    private var isAwaitingBottomActionState = false
    private val feeAdapter by lazy {
        BorrowingFeeBreakdownAdapter()
    }
    private val installAdapter by lazy {
        BorrowingScheduleAdapter().apply {
            setOnItemClickListener { item, position ->
                val lastDueIndex = items.indexOfLast { it1 -> it1.isDue() }
                val firstProcessIndex = items.indexOfFirst { it1 -> it1.isProcess() }
//                if (item.isDueAndSettle()) return@setOnItemClickListener
                items.forEachIndexed { i, t ->
                    t.isSelect = i <= position
                    if (t.isDueAndSettle()) {
                        t.isSelect = true
                    }
                }
//                if (firstProcessIndex >= 0) {
//                    items[firstProcessIndex].isSelect = true
//                }
//                if (lastDueIndex >= 0 && lastDueIndex + 1 < items.size) {
//                    items[lastDueIndex + 1].isSelect = true
//                }
                if (item.isDueAndSettle()) {
                    item.isSelect = true
                }
                notifyItemRangeChanged(0, itemCount, 0)
                binding.tvSelectAmount.text =
                    items.filter { it1 -> !it1.isSettle() && it1.isSelect }
                        .fold(
                            BigDecimal.ZERO
                        ) { acc, order ->
                            acc + (order.actualNeedRepayAmount
                                ?: BigDecimal.ZERO)
                        }
                        .formatAmountWithPrefix(orderDetail?.appOrderInfoDto?.currencySymbol)
            }
        }
    }

    override fun initView() {
        trackOrderDetailPage()
        configureDetailViews()
        connectPaymentEntry()
        connectRenewalControls()
    }

    /** Reports the detail-page tracking event and wires up the retry action. */
    private fun trackOrderDetailPage() = with(binding) {
        trackEvent(LOAN_ORDER_CONFIRMATION_PAGE)
        pageState.setOnRetryClickListener {
            vm.getOrderDetail(orderId)
        }
    }

    /** Binds adapters, the clickable agreement text, and the shortcut buttons in state 1. */
    private fun configureDetailViews() = with(binding) {
        rvFee.adapter = feeAdapter
        contractLayout.setSpannableClickableTexts(
            String.format(
                getString(R.string.product_detail_agreement),
                getString(R.string.lease_contract)
            ),
            listOf(
                ClickablePart(
                    getString(R.string.lease_contract),
                    resolveColorCompat(R.color.brand_primary),
                ) {
                    showLoanAgreement(getString(R.string.lease_contract), PRODUCT_AGREEMENT)
                },
            ),
        )
        rvPlan.adapter = installAdapter
        state1Repay.singleClick { tvRepay.performClick() }
        state1Borrow.singleClick { tvBorrow.performClick() }
    }

    private fun connectPaymentEntry() = with(binding) {
        tvApply.singleClick {
            val payGoUrl = orderDetail?.appOrderRepayDto?.payGoUrl
            if (payGoUrl.isNullOrBlank()) {
                getString(R.string.payment_tip).showToastMessage()
            } else {
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageRepaymentLink,
                        act = ACT_inRepaymentLink,
                        result = System.currentTimeMillis().toString()
                    )
                )
                ContentBrowserActivity.launch(
                    this@RecordDetailActivity, getString(R.string.repayment), payGoUrl
                )
            }
        }
    }

    private fun connectRenewalControls() = with(binding) {
        tvRepay.singleClick {
            if (detailLayout.isVisible && installAdapter.items.none { it1 -> !it1.isSettle() && it1.isSelect }) {
                getString(R.string.toast_repayment_select).showToastMessage()
                return@singleClick
            }
            if (currentButtonSign == "2" || currentButtonSign == "3"|| currentButtonSign == "4") {
                vm.cancelApply(orderId) {
                    proceedWithSelectedRepayment()
                }
            } else {
                proceedWithSelectedRepayment()
            }
        }
        tvBorrow.singleClick {
            if (shouldBlockUncheckedAgreement()) {
                getString(R.string.toast_repay_auto_apply_agreement).showToastMessage()
                return@singleClick
            }
            if (detailLayout.isVisible && installAdapter.items.none { it1 -> !it1.isSettle() && it1.isSelect }) {
                getString(R.string.toast_repayment_select).showToastMessage()
                return@singleClick
            }
            if (currentButtonSign == "1") {
                vm.repayAndBorrow(orderId, 1) {
                    proceedWithSelectedRepayment()
                }
            } else if (tvBorrow.text.toString() == getString(R.string.repay)) {
                tvRepay.performClick()
            } else {
                showRepayAndReapplyDialog(
                    isDue = tvBorrow.isSelected,
                    confirmAction = {
                        vm.repayAndBorrow(orderId, 1) {
                            proceedWithSelectedRepayment()
                        }
                    }
                )
            }
        }
        tvBorrowAll.singleClick {
            if (detailLayout.isVisible && installAdapter.items.none { item -> !item.isSettle() && item.isSelect }) {
                getString(R.string.toast_repayment_select).showToastMessage()
                return@singleClick
            }
            showRepayAndReapplyDialog(
                isDue = tvBorrow.isSelected,
                isApplyAll = true,
                confirmAction = {
                    vm.repayAndBorrow(orderId, 2) {
                        proceedWithSelectedRepayment()
                    }
                },
            )
        }
        cbAutoApply.setOnClickListener {
            cbAutoApply.isSelected = !cbAutoApply.isSelected
            refreshAutoApplyButtons()
        }
        tvPrivacy.setOnClickListener {
            cbAutoApply.isSelected = !cbAutoApply.isSelected
            refreshAutoApplyButtons()
        }
    }

    private fun proceedWithSelectedRepayment() = with(binding) {
        if (detailLayout.isVisible) {
            vm.installmentRepay(
                orderNo = orderDetail?.appOrderInfoDto?.orderNo,
                planNumberList = installAdapter.items
                    .filter { item -> !item.isSettle() && item.isSelect }
                    .map { it.planPart },
            )
        } else {
            tvApply.performClick()
        }
    }

    /** States 3/4 switch between plain repayment and auto-apply actions via the checkbox. */
    private fun refreshAutoApplyButtons() = with(binding) {
        if (currentButtonSign == "3" || currentButtonSign == "4") {
            tvBorrow.text = getString(
                if (cbAutoApply.isSelected) R.string.repay_auto_apply else R.string.repay
            )
            tvBorrowAll.isVisible = currentButtonSign == "4" && cbAutoApply.isSelected
            tvBorrowTip.isVisible = currentButtonSign == "3" && cbAutoApply.isSelected
            tvBorrowAllTip.isVisible = currentButtonSign == "4" && cbAutoApply.isSelected
            updateBottomActionColors(isCurrentOrderDue)
        }
    }

    override fun onResume() {
        super.onResume()
        vm.getOrderDetail(orderId)
    }

    private fun showLoanAgreement(title: String, baseUrl: String) {
        ContentBrowserActivity.launch(
            this,
            title,
            baseUrl + "userId=${orderDetail?.appOrderInfoDto?.userId}&productId=${orderDetail?.appOrderInfoDto?.productId}&amount=${orderDetail?.appOrderInfoDto?.loanAmount.toString()}"
        )
    }

    private var orderDetail: RecordDetailResponse? = null
    private fun renderOrderDetail(detail: RecordDetailResponse) = with(binding) {
        orderDetail = detail
        installGroup.isVisible = false
        val plans = detail.installmentRepaymentPlanDTOList.orEmpty()
        val lastDueIndex = plans.indexOfLast { it.isDueAndSettle() }
        val firstProcessIndex = plans.indexOfFirst { it.isProcess() }
        installAdapter.submitItems(plans.onEachIndexed { index, it1 ->
//                            it1.planStatus =34
            it1.isSelect =
                (index == lastDueIndex + 1) || index == firstProcessIndex
            if (it1.isDueAndSettle()) {
                it1.isSelect = true
            }
            it1.isExpend = it1.isSelect
        })
        val hasInstallments = plans.isNotEmpty()
        detailLayout.isVisible = hasInstallments
        detail.appOrderInfoDto?.let { order ->
            tvLoanAmount.text =
                String.format(getString(R.string.loan_amount), order.currency)
            tvAmount.text =
                order.loanAmount.formatAmountWithPrefix(order.currencySymbol)
            tvModel.text = "${Build.BRAND} ${Build.MODEL}"
            tvProductName.text = order.productName
            tvOrderNo.text = order.orderNo
            tvOrderStatus.setTextColor(resolveColorCompat(R.color.brand_primary))
            when (order.status) {
                ORDER_STATUS_SUCCESS,
                ORDER_STATUS_REVIEW,
                ORDER_STATUS_AUTO,
                ORDER_STATUS_MANUAL,
                ORDER_STATUS_CASH,
                ORDER_STATUS_PAYMENT_ING,
                ORDER_STATUS_PAYMENT_FAIL,
                    -> {
                    detailLayout.isVisible = false
                    tvOrderStatus.text = getString(R.string.pending_cash)
                }

                ORDER_STATUS_PAYMENT_PROCESS -> {
                    tvOrderStatus.text =
                        getString(R.string.repayment_processing)
                }

                ORDER_STATUS_PAYMENT_PENDING,
                ORDER_STATUS_IN_RENEWAL,
                ORDER_STATUS_IN_RENEWAL_PROCESS,
                    -> {
                    tvOrderStatus.text = getString(R.string.pending_repayment)
                }

                ORDER_STATUS_OVERDUE,
                ORDER_STATUS_BAD_DEBTS,
                    -> {
                    tvOrderStatus.text = getString(R.string.overdue)
                    tvOrderStatus.setTextColor(resolveColorCompat(R.color.status_error))
                }

                ORDER_STATUS_AUTO_FAIL,
                ORDER_STATUS_MANUAL_FAIL,
                    -> {
                    tvOrderStatus.text = getString(R.string.reject)
                }

                ORDER_STATUS_CLOSE,
                ORDER_STATUS_INVALID,
                    -> {
                    tvOrderStatus.text = getString(R.string.closed)
                }

                ORDER_STATUS_SETTLE,
                ORDER_STATUS_SETTLE_REDUCE,
                ORDER_STATUS_SETTLE_RENEWAL,
                ORDER_STATUS_SETTLE_REDUCE_OR_RENEWAL,
                    -> {
                    tvOrderStatus.text = getString(R.string.complete)
                }
            }
            tvOrderStatusTop.text = tvOrderStatus.text
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageOrderDetail,
                    act = ACT_inOrdersDetail,
                    result = System.currentTimeMillis()
                        .toString() + "|" + order.orderId + "|" + order.status
                )
            )
            tvApplyDate.text = detail.applyDateStr ?: "-"
            tvLoanDate.text = detail.loanDateStr ?: "-"
            tvDueDate.text = detail.shouldRepayDateStr ?: "-"
            val loanPeriod = String.format(
                getString(R.string.num_days),
                order.timeLimit.toString()
            )
            tvHeaderDays.text = loanPeriod

            tvInterestTitle.text =
                String.format(
                    getString(R.string.interest_day),
                    detail.dayRateStr + "%"
                )
            tvInterest.text =
                detail.interestAmount.formatAmountWithPrefix(order.currencySymbol)
            tvActuallyAmount.text =
                detail.actualAmount.formatAmountWithPrefix(order.currencySymbol)
            tvInstallFee.text =
                detail.totalInstallmentServiceFee.formatAmountWithPrefix(order.currencySymbol)
            installGroup.isVisible = detail.totalInstallmentServiceFee.isPositive()
            tvAccount.text = detail.bankNo
            feeAdapter.currencySymbol = order.currencySymbol
            feeAdapter.submitItems(order.orderHandleFees)
            tvTotalRepayTitle.text =
                String.format(
                    getString(R.string.total_repayment_str),
                    order.currency
                )
            tvTotalRepay.text =
                detail.actualNeedRepayAmount.formatAmountWithPrefix(order.currencySymbol)
            val isDue =
                order.status == ORDER_STATUS_OVERDUE || order.status == ORDER_STATUS_BAD_DEBTS
            isCurrentOrderDue = isDue
            tvDueFee.isVisible = isDue
            tvDueFeeTitle.isVisible = isDue
            tvDueFee.text =
                detail.appOrderRepayDto?.penaltyAmount.formatAmountWithPrefix(
                    null
                )
            val headerCornerRadius = resources.getDimension(R.dimen.dp_16)
            headerMainLayout.setRoundedRectangleBackground(
                solidColor = resolveColorCompat(
                    if (isDue) R.color.action_withdraw else R.color.brand_primary
                ),
                leftTopRadius = headerCornerRadius,
                rightTopRadius = headerCornerRadius,
            )
            headerInfoLayout.setRoundedRectangleBackground(
                solidColor = resolveColorCompat(
                    if (isDue) R.color.color_order_over else R.color.aide_color
                ),
                rightBottomRadius = headerCornerRadius,
                leftBottomRadius = headerCornerRadius,
            )
            tvOrderStatusTop.isSelected = isDue
            tvApply.isSelected = isDue
            tvBorrow.isSelected = isDue
            tvRepay.isSelected = isDue
            updateBottomActionColors(isDue)
            hideBottomActionPanel()
            when (order.status) {
                ORDER_STATUS_PAYMENT_PENDING,
                ORDER_STATUS_IN_RENEWAL,
                ORDER_STATUS_IN_RENEWAL_PROCESS,
                ORDER_STATUS_OVERDUE,
                ORDER_STATUS_BAD_DEBTS,
                    -> {
                    if (!isFromBatch) {
                        val selectedAmount =
                            if (detailLayout.isVisible) {
                                installAdapter.items.filter { it1 -> !it1.isSettle() && it1.isSelect }
                                    .fold(
                                        BigDecimal.ZERO
                                    ) { acc, order ->
                                        acc + (order.actualNeedRepayAmount
                                            ?: BigDecimal.ZERO)
                                    }
                                    .formatAmountWithPrefix(order.currencySymbol)
                            } else {
                                detail.actualNeedRepayAmount.formatAmountWithPrefix(order.currencySymbol)
                            }
                        presentBorrowingActions(
                            selectedAmount = selectedAmount
                        )
                        vm.getButtonState()
                    }
                }

                else -> {
                    tvApply.isVisible = false
                }
            }
        }
    }

    override fun initObserve() = with(vm) {
        super.initObserve()

        orderDetailState.observe(this@RecordDetailActivity) { state ->
            render(state)
        }
        buttonResult.observe(this@RecordDetailActivity) { sign ->
            if (isAwaitingBottomActionState) {
                renderRepaymentActionState("4")
                binding.bottomActionLayout.visibility = View.VISIBLE
                isAwaitingBottomActionState = false
            }
        }
        installmentRepayResult.observe(this@RecordDetailActivity) {
            openRepaymentPage(it?.payUrl)
        }
    }

    private fun render(state: PageLoadState<RecordDetailResponse>) = with(binding) {
        contentLayout.isVisible = state is PageLoadState.Content
        when (state) {
            PageLoadState.Loading -> pageState.showLoading()
            PageLoadState.Error -> pageState.showError()
            PageLoadState.Empty -> Unit
            is PageLoadState.Content -> {
                renderOrderDetail(state.data)
                pageState.hide()
            }
        }
    }

    private fun openRepaymentPage(payUrl: String?) {
        if (!payUrl.isNullOrBlank()) {
            ContentBrowserActivity.launch(this, getString(R.string.repayment), payUrl)
            return
        }
        getString(R.string.payment_tip).showToastMessage()
    }

    fun scrollToRepaymentOptions() {
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun hideBottomActionPanel() {
        currentButtonSign = null
        isAwaitingBottomActionState = false
        binding.bottomActionLayout.isVisible = false
    }

    private fun presentBorrowingActions(
        selectedAmount: String,
    ) = with(binding) {
        currentButtonSign = null
        isAwaitingBottomActionState = true
        // Keep the bottom area reserved until the server state is known, avoiding a default-state flash.
        bottomActionLayout.visibility = View.INVISIBLE
        cbAutoApply.isSelected = true
        tvSelectAmount.text = selectedAmount
    }

    /** Applies state 0-4 to the fixed bottom layout without rearranging constraints. */
    private fun renderRepaymentActionState(sign: String?) = with(binding) {
        val normalizedSign = when (sign) {
            "0", "1", "2", "3", "4" -> sign
            else -> "2"
        }
        currentButtonSign = normalizedSign
        syncBottomVisibility(normalizedSign)
        when (normalizedSign) {
            "0" -> tvRepay.text = getString(R.string.repay)
            "1" -> {
                tvRepay.text = getString(R.string.repay)
            }
            "3", "4" -> refreshAutoApplyButtons()
            else -> {
                tvRepay.text = getString(R.string.repay)
                tvBorrow.text = getString(R.string.repay_auto_apply)
            }
        }
        updateBottomActionColors(isCurrentOrderDue)
    }

    private fun syncBottomVisibility(sign: String?) = with(binding) {
        val isState0 = sign == "0"
        val isState1 = sign == "1"
        val isState3 = sign == "3"
        val isState4 = sign == "4"
        val isState2 = !isState0 && !isState1 && !isState3 && !isState4
        val showSelectedAmount = detailLayout.isVisible
        tvSelect.isVisible = showSelectedAmount
        tvSelectAmount.isVisible = showSelectedAmount
        state1ActionLayout.isVisible = isState1
        state1BorrowTip.isVisible = isState1
        tvRepay.isVisible = isState0 || isState2
        cbAutoApply.isVisible = isState1 || isState2 || isState3 || isState4
        tvPrivacy.isVisible = isState1 || isState2 || isState3 || isState4
        tvBorrow.isVisible = !isState0 && !isState1
        tvBorrowAll.isVisible = isState4 && cbAutoApply.isSelected
        tvBorrowTip.isVisible = isState2 || (isState3 && cbAutoApply.isSelected)
        tvBorrowAllTip.isVisible = isState4 && cbAutoApply.isSelected
    }

    // States 1 and 2 require agreement before Repay & Auto-Apply can continue.
    private fun shouldBlockUncheckedAgreement(): Boolean =
        (currentButtonSign == "1" || currentButtonSign == "2") &&
            !binding.cbAutoApply.isSelected

    private fun updateBottomActionColors(isDue: Boolean) = with(binding) {
        val actionColor = resolveColorCompat(if (isDue) R.color.status_error else R.color.brand_primary)
        val borrowColor =
            if (currentButtonSign == "2" || currentButtonSign == "3") {
                resolveColorCompat(R.color.brand_primary)
            } else {
                actionColor
            }
        tvRepay.updateAppearance(
            variant = StatefulActionButton.VARIANT_OUTLINE,
            strokeColor = actionColor,
            textColor = actionColor,
        )
        if (isDue) {
            tvBorrow.updateAppearance(
                variant = StatefulActionButton.VARIANT_FILLED,
                solidColor = actionColor,
                textColor = resolveColorCompat(R.color.text_inverse),
            )
            return@with
        }
        val showRepayStyle =
            (currentButtonSign == "3" || currentButtonSign == "4") && !cbAutoApply.isSelected
        if (showRepayStyle) {
            tvBorrow.updateAppearance(
                variant = StatefulActionButton.VARIANT_OUTLINE,
                strokeColor = actionColor,
                textColor = actionColor,
            )
        } else {
            tvBorrow.updateAppearance(
                variant = StatefulActionButton.VARIANT_FILLED,
                solidColor = borrowColor,
                textColor = resolveColorCompat(R.color.text_inverse),
            )
        }
    }
}
