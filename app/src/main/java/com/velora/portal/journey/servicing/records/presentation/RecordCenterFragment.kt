package com.velora.portal.journey.servicing.records.presentation

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseFragment
import com.velora.portal.platform.common.data.ACT_inOrdersPage
import com.velora.portal.platform.common.data.PageOrder
import com.velora.portal.platform.common.data.ORDER_STATUS_IN_RENEWAL
import com.velora.portal.platform.common.data.ORDER_STATUS_IN_RENEWAL_PROCESS
import com.velora.portal.platform.common.data.ORDER_STATUS_OVERDUE
import com.velora.portal.platform.common.data.ORDER_STATUS_PAYMENT_PENDING
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.databinding.FragmentRecordCenterBinding
import com.velora.portal.domain.credit.model.MemberOverviewResponse
import com.velora.portal.journey.lending.catalog.presentation.LoanDashboardViewModel
import com.velora.portal.journey.servicing.checkout.presentation.BulkRepaymentActivity
import com.velora.portal.journey.servicing.records.presentation.adapter.HomeOrderAdapter
import com.velora.portal.platform.design.extension.setClickableTextWithScale
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

class RecordCenterFragment : BaseFragment<FragmentRecordCenterBinding>(R.layout.fragment_record_center) {
    override val binding by viewBinding(FragmentRecordCenterBinding::bind)
    private val vm by viewModels<LoanDashboardViewModel>()

    private val orderAdapter by lazy {
        HomeOrderAdapter().apply {
            setOnItemClickListener { item, _ ->
                context.start<LoanRecordDetailActivity> {
                    putExtra("orderId", item.orderId)
                    putExtra("isFromBatch", false)
                }
            }
        }
    }

    override fun initView() {
        bindOrderList()
        connectRefreshAndRepayment()
    }

    /** Binds the order adapter and wires up the retry action of the page state view. */
    private fun bindOrderList() = with(binding) {
        rvOrder.adapter = orderAdapter
        pageState.setOnRetryClickListener {
            vm.getAuthData()
        }
    }

    /** Connects the repayment shortcut and the pull-to-refresh gesture. */
    private fun connectRefreshAndRepayment() = with(binding) {
        tvRepayment.singleClick {
            it.context.start<BulkRepaymentActivity>()
        }
        swipeRefreshLayout.setOnRefreshListener {
            vm.getAuthData()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.getAuthData()
        vm.submitTrackingEvent(
            TrackBean(
                p = PageOrder,
                act = ACT_inOrdersPage,
                result = System.currentTimeMillis().toString()
            )
        )
    }

    override fun initObserve() =with(vm){
        authDataState.observe(this@RecordCenterFragment) { state ->
            render(state)
        }
    }

    private fun render(state: PageLoadState<MemberOverviewResponse>) = with(binding) {
        when (state) {
            PageLoadState.Loading -> {
                contentLayout.isVisible = false
                pageState.showLoading()
            }

            PageLoadState.Error -> {
                swipeRefreshLayout.isRefreshing = false
                contentLayout.isVisible = false
                pageState.showError()
            }

            PageLoadState.Empty -> Unit

            is PageLoadState.Content -> {
                swipeRefreshLayout.isRefreshing = false
                contentLayout.isVisible = true
                pageState.hide()
                val response = state.data
                binding.apply {
                    val orderList = response.repayProducts.orEmpty()
                    emptyOrder.isVisible = orderList.isEmpty()
                    orderLayout.isVisible = orderList.isNotEmpty()
                    orderAdapter.submitItems(orderList)
                    val size = orderList.count { order ->
                        order.orderStatus == ORDER_STATUS_PAYMENT_PENDING ||
                            order.orderStatus == ORDER_STATUS_IN_RENEWAL ||
                            order.orderStatus == ORDER_STATUS_IN_RENEWAL_PROCESS ||
                            order.orderStatus == ORDER_STATUS_OVERDUE
                    }
                    tvOrderNum.setClickableTextWithScale(
                        String.format(getString(R.string.home_order_num), size.toString()),
                        size.toString(),
                        root.context.resolveColorCompat(R.color.action_withdraw),
                    )
                    repaymentLayout.isVisible = response.showMultipleRepaySign == 1 && size > 0
                }
            }
        }
    }
}
