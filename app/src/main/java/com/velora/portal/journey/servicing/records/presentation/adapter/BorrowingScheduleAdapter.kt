package com.velora.portal.journey.servicing.records.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.domain.credit.model.CatalogPlanBean
import com.velora.portal.databinding.ItemRecordInstallmentBinding
import com.velora.portal.journey.servicing.records.presentation.LoanRecordDetailActivity
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

class BorrowingScheduleAdapter :
    BaseAdapter<CatalogPlanBean, ItemRecordInstallmentBinding>(ItemRecordInstallmentBinding::inflate) {

    override fun bindItem(
        binding: ItemRecordInstallmentBinding,
        item: CatalogPlanBean,
        position: Int,
    ) = with(binding) {
        tvDate.text = item.repayTime?.substringBefore(" ")
        tvAmount.text = item.actualNeedRepayAmount.formatAmountWithPrefix()
        tvLoanAmount.text = item.needRepayLoanAmount.formatAmountWithPrefix()
        tvInterest.text = item.needRepayInterestSum.formatAmountWithPrefix()
        tvServiceFee.text = item.needRepayAfterHandleAmount.formatAmountWithPrefix()
        tvDueFee.text = item.needRepayPenaltyAmount.formatAmountWithPrefix()
        detailLayout.isVisible = item.isExpend
        ivArrow.rotation = if (item.isExpend) 180f else 0f
        tvStatus.setOnClickListener { ivArrow.performClick() }
        ivArrow.setOnClickListener {
            detailLayout.isVisible = !detailLayout.isVisible
            ivArrow.rotation = if (detailLayout.isVisible) 180f else 0f
            if (context is LoanRecordDetailActivity?) {
                (context as LoanRecordDetailActivity?)?.scrollToRepaymentOptions()
            }
            item.isExpend = detailLayout.isVisible
        }
        ivCheck.setImageResource(
            if (item.isSelect) (if (item.isProcess()) R.mipmap.icon_account_select else R.mipmap.icon_account_select)
            else R.mipmap.icon_account_unselect
        )
        val isSelect =
            if (position == items.size - 1) item.isSelect else (items[position + 1].isSelect)
        tvStatus.setTextColor(context.resolveColorCompat(R.color.text_body))
        repaymentTimelineConnector.isVisible = position != items.size - 1
        dueFeeItem.isVisible = false
        when (item.planStatus) {
            34, 35 -> {
                dueFeeItem.isVisible = true
                tvStatus.text = context.getString(R.string.overdue)
                tvStatus.setTextColor(context.resolveColorCompat(R.color.status_error))
            }

            31 -> {
                tvStatus.text = context.getString(R.string.processing)
                tvStatus.setTextColor(context.resolveColorCompat(R.color.status_success))
            }

            30, 32, 35 -> {
                tvStatus.text = context.getString(R.string.pending)
                tvStatus.setTextColor(context.resolveColorCompat(R.color.status_success))
            }

            40, 41, 42, 43 -> {
                tvStatus.text = context.getString(R.string.settled)
//                    tvDate.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                    tvAmount.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                tvStatus.setTextColor(context.resolveColorCompat(R.color.text_body))
            }

            14, 15 -> {
                tvStatus.text = context.getString(R.string.rejected)
//                    tvDate.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                    tvAmount.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                tvStatus.setTextColor(context.resolveColorCompat(R.color.text_body))
            }

            23 -> {
                tvStatus.text = context.getString(R.string.closed)
//                    tvDate.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                    tvAmount.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                tvStatus.setTextColor(context.resolveColorCompat(R.color.text_body))
            }

            22 -> {
                tvStatus.text = context.getString(R.string.invalid)
//                    tvDate.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                    tvAmount.setTextColor(context.resolveColorCompat(R.color.C_75707E))
//                tvStatus.setTextColor(context.resolveColorCompat(R.color.text_body))
            }
        }
    }

}
