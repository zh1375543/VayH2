package com.velora.portal.feature.records.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseAdapter
import com.velora.portal.core.common.data.ORDER_STATUS_AUTO
import com.velora.portal.core.common.data.ORDER_STATUS_BAD_DEBTS
import com.velora.portal.core.common.data.ORDER_STATUS_CASH
import com.velora.portal.core.common.data.ORDER_STATUS_IN_RENEWAL
import com.velora.portal.core.common.data.ORDER_STATUS_IN_RENEWAL_PROCESS
import com.velora.portal.core.common.data.ORDER_STATUS_MANUAL
import com.velora.portal.core.common.data.ORDER_STATUS_OVERDUE
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_FAIL
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_ING
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_PENDING
import com.velora.portal.core.common.data.ORDER_STATUS_PAYMENT_PROCESS
import com.velora.portal.core.common.data.ORDER_STATUS_REVIEW
import com.velora.portal.core.common.data.ORDER_STATUS_SUCCESS
import com.velora.portal.databinding.ItemHomeBorrowingBinding
import com.velora.portal.feature.catalog.model.CatalogItemBean
import com.velora.portal.core.common.util.text.formatAmountWithPrefix

class HomeOrderAdapter :
    BaseAdapter<CatalogItemBean, ItemHomeBorrowingBinding>(ItemHomeBorrowingBinding::inflate) {

    override fun bindItem(
        binding: ItemHomeBorrowingBinding,
        item: CatalogItemBean,
        position: Int,
    ) = with(binding) {
        tvRepay.isSelected = false
        tvState.isSelected = false
        tvDesc.isSelected = false
        tvName.text = item.productName
        when (item.orderStatus) {
            ORDER_STATUS_SUCCESS,
            ORDER_STATUS_REVIEW,
            ORDER_STATUS_AUTO,
            ORDER_STATUS_MANUAL,
            ORDER_STATUS_CASH,
            ORDER_STATUS_PAYMENT_ING,
            ORDER_STATUS_PAYMENT_FAIL,
                -> {
                tvState.text = context.getString(R.string.pending_cash)
                tvDesc.text = context.getString(R.string.pending_cash_desc)
                tvRepay.isVisible = false
                tvAmountTitle.text = context.getString(R.string.l_amount)
                tvLoanAmount.text = item.loanAmount.formatAmountWithPrefix(item.currencySymbol)
                tvDateTitle.text = context.getString(R.string.apply_date)
                tvDate.text = item.applyDateStr
            }

            ORDER_STATUS_PAYMENT_PROCESS -> {
                tvState.text = context.getString(R.string.repayment_processing)
                tvDesc.text = context.getString(R.string.pending_repayment_desc)
                tvRepay.isVisible = false
                tvAmountTitle.text = context.getString(R.string.total_repayment)
                tvLoanAmount.text = item.actualRepayAmount.formatAmountWithPrefix(item.currencySymbol)
                tvDateTitle.text = context.getString(R.string.due_date)
                tvDate.text = item.repayTimeStr
            }

            ORDER_STATUS_PAYMENT_PENDING,
            ORDER_STATUS_IN_RENEWAL,
            ORDER_STATUS_IN_RENEWAL_PROCESS,
                -> {
                tvState.text = context.getString(R.string.pending_repayment)
                tvDesc.text = context.getString(R.string.pending_repayment_desc)
                tvRepay.isVisible = true
                tvAmountTitle.text = context.getString(R.string.total_repayment)
                tvLoanAmount.text = item.actualRepayAmount.formatAmountWithPrefix(item.currencySymbol)
                tvDateTitle.text = context.getString(R.string.due_date)
                tvDate.text = item.repayTimeStr
            }

            ORDER_STATUS_OVERDUE,
            ORDER_STATUS_BAD_DEBTS,
                -> {
                tvState.isSelected = true
                tvDesc.isSelected = true
                tvRepay.isSelected = true
                tvState.text = context.getString(R.string.overdue)
                tvDesc.text = context.getString(R.string.overdue_desc)
                tvRepay.isVisible = true
                tvAmountTitle.text = context.getString(R.string.total_repayment)
                tvLoanAmount.text = item.actualRepayAmount.formatAmountWithPrefix(item.currencySymbol)
                tvDateTitle.text = context.getString(R.string.due_date)
                tvDate.text = item.repayTimeStr
            }
        }
        tvAmountTitle.text = buildString {
            append(tvAmountTitle.text.toString())
            append(":")
        }
        tvDateTitle.text = buildString {
            append(tvDateTitle.text.toString())
            append(":")
        }
    }
}
