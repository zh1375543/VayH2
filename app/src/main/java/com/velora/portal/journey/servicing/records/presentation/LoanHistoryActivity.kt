package com.velora.portal.journey.servicing.records.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.databinding.ScreenLoanHistoryBinding
import com.velora.portal.platform.common.data.ACT_inOrderHistory
import com.velora.portal.platform.common.data.PageHistory
import com.velora.portal.domain.credit.model.LoanRecordItem
import com.velora.portal.journey.servicing.records.presentation.adapter.BorrowingHistoryAdapter
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

class LoanHistoryActivity : BaseActivity<ScreenLoanHistoryBinding>() {

    override val binding by viewBinding(ScreenLoanHistoryBinding::inflate)
    private val vm by viewModels<RecordCenterViewModel>()

    private val orderAdapter by lazy {
        BorrowingHistoryAdapter().apply {
            setOnItemClickListener { _, position ->
                start<LoanRecordDetailActivity> {
                    putExtra("orderId", items[position].id)
                    putExtra("isFromBatch", false)
                }
            }
        }
    }

    override fun initView() {
        reportHistoryEntry()
        setupHistoryList()
    }

    /** Report page-entry tracking event for the order history screen. */
    private fun reportHistoryEntry() {
        vm.submitTrackingEvent(
            TrackBean(
                p = PageHistory,
                act = ACT_inOrderHistory,
                result = System.currentTimeMillis().toString()
            )
        )
    }

    /** Wire up the order list adapter and the retry action for the page state view. */
    private fun setupHistoryList() = with(binding) {
        pageState.setOnRetryClickListener {
            vm.getOrderList()
        }
        rvOrder.adapter = orderAdapter
    }

    override fun onResume() {
        super.onResume()
        vm.getOrderList()
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        borrowingHistoryUiState.observe(this@LoanHistoryActivity) {
            renderHistoryState(it)
        }
    }

    private fun renderHistoryState(state: PageLoadState<List<LoanRecordItem>>) = with(binding) {
        rvOrder.isVisible = state is PageLoadState.Content
        when (state) {
            PageLoadState.Loading -> pageState.showLoading()
            PageLoadState.Error -> pageState.showError()

            PageLoadState.Empty -> {
                if (orderAdapter.items.isNotEmpty()) {
                    orderAdapter.submitItems(emptyList())
                }
                pageState.showEmpty(R.mipmap.icon_order_empy, R.string.order_empty)
            }

            is PageLoadState.Content -> {
                if (orderAdapter.items != state.data) {
                    orderAdapter.submitItems(state.data)
                }
                pageState.hide()
            }
        }
    }
}
