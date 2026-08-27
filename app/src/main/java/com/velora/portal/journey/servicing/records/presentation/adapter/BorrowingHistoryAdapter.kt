package com.velora.portal.journey.servicing.records.presentation.adapter

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.platform.common.data.ORDER_STATUS_AUTO
import com.velora.portal.platform.common.data.ORDER_STATUS_AUTO_FAIL
import com.velora.portal.platform.common.data.ORDER_STATUS_BAD_DEBTS
import com.velora.portal.platform.common.data.ORDER_STATUS_CASH
import com.velora.portal.platform.common.data.ORDER_STATUS_CLOSE
import com.velora.portal.platform.common.data.ORDER_STATUS_INVALID
import com.velora.portal.platform.common.data.ORDER_STATUS_IN_RENEWAL
import com.velora.portal.platform.common.data.ORDER_STATUS_IN_RENEWAL_PROCESS
import com.velora.portal.platform.common.data.ORDER_STATUS_MANUAL
import com.velora.portal.platform.common.data.ORDER_STATUS_MANUAL_FAIL
import com.velora.portal.platform.common.data.ORDER_STATUS_OVERDUE
import com.velora.portal.platform.common.data.ORDER_STATUS_PAYMENT_FAIL
import com.velora.portal.platform.common.data.ORDER_STATUS_PAYMENT_ING
import com.velora.portal.platform.common.data.ORDER_STATUS_PAYMENT_PENDING
import com.velora.portal.platform.common.data.ORDER_STATUS_PAYMENT_PROCESS
import com.velora.portal.platform.common.data.ORDER_STATUS_REVIEW
import com.velora.portal.platform.common.data.ORDER_STATUS_SETTLE
import com.velora.portal.platform.common.data.ORDER_STATUS_SETTLE_REDUCE
import com.velora.portal.platform.common.data.ORDER_STATUS_SETTLE_REDUCE_OR_RENEWAL
import com.velora.portal.platform.common.data.ORDER_STATUS_SETTLE_RENEWAL
import com.velora.portal.platform.common.data.ORDER_STATUS_SUCCESS
import com.velora.portal.databinding.ItemRecordHistoryBinding
import com.velora.portal.domain.credit.model.LoanRecordItem
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

class BorrowingHistoryAdapter :
    BaseAdapter<LoanRecordItem, ItemRecordHistoryBinding>(ItemRecordHistoryBinding::inflate) {

    override fun bindItem(
        binding: ItemRecordHistoryBinding,
        item: LoanRecordItem,
        position: Int,
    ) = with(binding) {
        tvProductName.text = item.productName
        tvLoanAmount.text = root.context.getString(R.string.loan_amount, item.currency)
        tvDays.text = root.context.getString(R.string.num_days, item.timeLimit.toString())
        tvAmount.text = item.loanAmount.formatAmountWithPrefix(item.currencySymbol)
        tvDateTitle.text = root.context.getString(R.string.apply_date)
        tvDate.text = item.createTime?.substringBefore(' ')
        tvStatus.text = when (item.status) {
            ORDER_STATUS_SETTLE,
            ORDER_STATUS_SETTLE_REDUCE,
            ORDER_STATUS_SETTLE_RENEWAL,
            ORDER_STATUS_SETTLE_REDUCE_OR_RENEWAL -> root.context.getString(R.string.complete)

            ORDER_STATUS_SUCCESS,
            ORDER_STATUS_REVIEW,
            ORDER_STATUS_AUTO,
            ORDER_STATUS_MANUAL,
            ORDER_STATUS_CASH,
            ORDER_STATUS_PAYMENT_ING,
            ORDER_STATUS_PAYMENT_FAIL -> root.context.getString(R.string.pending_cash)

            ORDER_STATUS_AUTO_FAIL,
            ORDER_STATUS_MANUAL_FAIL -> root.context.getString(R.string.reject)

            ORDER_STATUS_CLOSE,
            ORDER_STATUS_INVALID -> root.context.getString(R.string.closed)

            ORDER_STATUS_PAYMENT_PROCESS -> root.context.getString(R.string.repayment_processing)

            ORDER_STATUS_PAYMENT_PENDING,
            ORDER_STATUS_IN_RENEWAL,
            ORDER_STATUS_IN_RENEWAL_PROCESS -> root.context.getString(R.string.pending_repayment)

            ORDER_STATUS_OVERDUE,
            ORDER_STATUS_BAD_DEBTS -> root.context.getString(R.string.overdue)

            else -> root.context.getString(R.string.pending_repayment)
        }
        val isOverdue = item.status == ORDER_STATUS_OVERDUE ||
            item.status == ORDER_STATUS_BAD_DEBTS
        val accentColor = root.context.resolveColorCompat(
            if (isOverdue) {
                R.color.status_error
            } else {
                R.color.brand_primary
            },
        )
        val cardRadius = root.resources.getDimension(R.dimen.dp_16)
        val borderWidth = root.resources.getDimensionPixelSize(R.dimen.dp_1)
        root.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cardRadius
            setColor(root.context.resolveColorCompat(R.color.surface_primary))
            setStroke(borderWidth, accentColor)
        }
        detailContainer.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(
                root.context.resolveColorCompat(
                    if (isOverdue) R.color.record_detail_overdue else R.color.record_detail_normal,
                ),
            )
            cornerRadii = floatArrayOf(
                0f, 0f, 0f, 0f,
                cardRadius - borderWidth, cardRadius - borderWidth,
                cardRadius - borderWidth, cardRadius - borderWidth,
            )
        }
        ivStatusBullet.imageTintList = ColorStateList.valueOf(accentColor)
        tvStatus.setTextColor(
            root.context.resolveColorCompat(
                if (isOverdue) R.color.status_error else R.color.text_primary,
            ),
        )
        tvDetail.setTextColor(accentColor)
    }
}
