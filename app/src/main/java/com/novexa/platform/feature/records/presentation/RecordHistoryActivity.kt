package com.novexa.platform.feature.records.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.databinding.ActivityRecordHistoryBinding
import com.novexa.platform.core.common.data.ACT_inOrderHistory
import com.novexa.platform.core.common.data.PageHistory
import com.novexa.platform.feature.records.model.RecordItemBean
import com.novexa.platform.feature.records.presentation.adapter.BorrowingHistoryAdapter
import com.novexa.platform.core.common.util.PageLoadState
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding

class RecordHistoryActivity : BaseActivity<ActivityRecordHistoryBinding>() {

    override val binding by viewBinding(ActivityRecordHistoryBinding::inflate)
    private val vm by viewModels<RecordCenterViewModel>()

    private val orderAdapter by lazy {
        BorrowingHistoryAdapter().apply {
            setOnItemClickListener { _, position ->
                start<RecordDetailActivity> {
                    putExtra("orderId", items[position].id)
                    putExtra("isFromBatch", false)
                }
            }
        }
    }

    override fun initView() {
        submitPageTracking()
        bindOrderListView()
    }

    /** Report page-entry tracking event for the order history screen. */
    private fun submitPageTracking() {
        vm.submitTrackingEvent(
            TrackBean(
                p = PageHistory,
                act = ACT_inOrderHistory,
                result = System.currentTimeMillis().toString()
            )
        )
    }

    /** Wire up the order list adapter and the retry action for the page state view. */
    private fun bindOrderListView() = with(binding) {
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
        borrowingHistoryUiState.observe(this@RecordHistoryActivity) {
            render(it)
        }
    }

    private fun render(state: PageLoadState<List<RecordItemBean>>) = with(binding) {
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
