package com.novexa.platform.feature.checkout.presentation.adapter

import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.core.common.data.ORDER_STATUS_AUTO
import com.novexa.platform.core.common.data.ORDER_STATUS_BAD_DEBTS
import com.novexa.platform.core.common.data.ORDER_STATUS_CASH
import com.novexa.platform.core.common.data.ORDER_STATUS_IN_RENEWAL
import com.novexa.platform.core.common.data.ORDER_STATUS_IN_RENEWAL_PROCESS
import com.novexa.platform.core.common.data.ORDER_STATUS_MANUAL
import com.novexa.platform.core.common.data.ORDER_STATUS_OVERDUE
import com.novexa.platform.core.common.data.ORDER_STATUS_PAYMENT_FAIL
import com.novexa.platform.core.common.data.ORDER_STATUS_PAYMENT_ING
import com.novexa.platform.core.common.data.ORDER_STATUS_PAYMENT_PENDING
import com.novexa.platform.core.common.data.ORDER_STATUS_PAYMENT_PROCESS
import com.novexa.platform.core.common.data.ORDER_STATUS_REVIEW
import com.novexa.platform.core.common.data.ORDER_STATUS_SUCCESS
import com.novexa.platform.feature.catalog.model.CatalogItemBean
import com.novexa.platform.databinding.ItemBulkRepaymentLoanBinding
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix

class BulkRepaymentLoanAdapter :
    BaseAdapter<CatalogItemBean, ItemBulkRepaymentLoanBinding>(ItemBulkRepaymentLoanBinding::inflate) {

    override fun bindItem(
        binding: ItemBulkRepaymentLoanBinding,
        item: CatalogItemBean,
        position: Int,
    ) = with(binding) {
        tvProductName.text = item.productName
        ivSelect.isSelected = item.isCheck
        tvRepayAmount.text = item.actualRepayAmount.formatAmountWithPrefix(item.currencySymbol)
        tvStatusValue.isSelected = false
        tvStatusValue.text = when (item.orderStatus) {
            ORDER_STATUS_SUCCESS,
            ORDER_STATUS_REVIEW,
            ORDER_STATUS_AUTO,
            ORDER_STATUS_MANUAL,
            ORDER_STATUS_CASH,
            ORDER_STATUS_PAYMENT_ING,
            ORDER_STATUS_PAYMENT_FAIL -> root.context.getString(R.string.pending_cash)

            ORDER_STATUS_PAYMENT_PROCESS -> root.context.getString(R.string.repayment_processing)

            ORDER_STATUS_PAYMENT_PENDING,
            ORDER_STATUS_IN_RENEWAL,
            ORDER_STATUS_IN_RENEWAL_PROCESS -> root.context.getString(R.string.pending_repayment)

            ORDER_STATUS_OVERDUE,
            ORDER_STATUS_BAD_DEBTS -> {
                tvStatusValue.isSelected = true
                root.context.getString(R.string.overdue)
            }

            else -> root.context.getString(R.string.overdue)
        }
    }

    override fun bindChildClickListeners(
        binding: ItemBulkRepaymentLoanBinding,
        item: CatalogItemBean,
        position: Int,
    ) {
        super.bindChildClickListeners(binding, item, position)
        binding.ivSelect.setOnClickListener {
            binding.root.performClick()
        }
        binding.btnProductDetail.setOnClickListener {
            dispatchChildClick(it, item, position)
        }
    }
}
