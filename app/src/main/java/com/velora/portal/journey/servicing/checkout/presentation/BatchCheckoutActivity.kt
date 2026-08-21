package com.velora.portal.journey.servicing.checkout.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivityBatchCheckoutBinding
import com.velora.portal.journey.servicing.records.presentation.RecordDetailActivity
import com.velora.portal.journey.servicing.checkout.presentation.adapter.BulkRepaymentLoanAdapter
import com.velora.portal.domain.credit.model.CatalogItemBean
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.browser.presentation.ContentBrowserActivity
import com.velora.portal.platform.common.util.viewBinding

class BatchCheckoutActivity :
    BaseActivity<ActivityBatchCheckoutBinding>() {

    override val binding by viewBinding(ActivityBatchCheckoutBinding::inflate)
    private val vm by viewModels<CheckoutViewModel>()

    private val orderAdapter by lazy {
        BulkRepaymentLoanAdapter().apply {
            configLoanItemClick(this)
        }
    }

    override fun initView() = with(binding) {
        setupLoanRecyclerView()
        setupSubmitButton()
        setupRetryHandler()
    }

    private fun setupLoanRecyclerView() = with(binding) {
        rvLoanList.adapter = orderAdapter
    }

    private fun setupSubmitButton() = with(binding) {
        btnSubmitRepayment.singleClick {
            if (orderAdapter.items.none { it1 -> it1.isCheck }) {
                getString(R.string.toast_empty_choose_repayment).showToastMessage()
                return@singleClick
            }
            vm.togetherRepayment(
                orderAdapter.items
                    .filter { it.isCheck }
                    .mapNotNull { it.orderNo }
            )
        }
    }

    private fun setupRetryHandler() = with(binding) {
        pageState.setOnRetryClickListener {
            loadOrderList()
        }
    }

    override fun onResume() {
        super.onResume()
        loadOrderList()
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        orderListState.observe(this@BatchCheckoutActivity) { state ->
            render(state)
        }
        selectedOrderAmount.observe(this@BatchCheckoutActivity) { amount ->
            binding.tvTotalAmount.text = amount
        }
        selectedOrderCount.observe(this@BatchCheckoutActivity) { count ->
            binding.tvSelectedCount.text = count
        }
        togetherRepayResult.observe(this@BatchCheckoutActivity) {
            val payUrl = it?.payUrl
            if (!payUrl.isNullOrBlank()) {
                ContentBrowserActivity.launch(
                    this@BatchCheckoutActivity,
                    getString(R.string.batch_repayment_orders),
                    payUrl
                )
                finish()
            } else {
                getString(R.string.payment_tip).showToastMessage()
            }
        }
    }

    private fun loadOrderList() {
        vm.getOrderList()
    }

    private fun configLoanItemClick(adapter: BulkRepaymentLoanAdapter) {
        adapter.setOnItemClickListener { item, position ->
            item.isCheck = !item.isCheck
            adapter.notifyItemRangeChanged(position, 1, 0)
            vm.updateBatchSelection(adapter.items)
        }
        adapter.setOnChildClickListener { view, item, _ ->
            if (view.id == R.id.btnProductDetail) {
                start<RecordDetailActivity> {
                    putExtra("orderId", item.orderId)
                    putExtra("isFromBatch", true)
                }
            }
        }
    }

    private fun render(state: PageLoadState<List<CatalogItemBean>>) = with(binding) {
        contentLayout.isVisible = state is PageLoadState.Content
        when (state) {
            PageLoadState.Loading -> pageState.showLoading()
            PageLoadState.Error -> pageState.showError()

            PageLoadState.Empty -> Unit

            is PageLoadState.Content -> {
                if (orderAdapter.items != state.data) {
                    orderAdapter.submitItems(state.data)
                }
                pageState.hide()
            }
        }
    }
}
