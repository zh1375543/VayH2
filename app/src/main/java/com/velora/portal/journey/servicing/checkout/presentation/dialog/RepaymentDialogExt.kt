package com.velora.portal.journey.servicing.checkout.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseDialog
import com.velora.portal.databinding.DialogNoActiveRepaymentBinding
import com.velora.portal.databinding.DialogRepayAndReapplyBinding
import com.velora.portal.application.PortalHostActivity
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.design.component.StatefulActionButton
import com.velora.portal.platform.common.util.showToastMessage

fun Context.showRepayAndReapplyDialog(
    isDue: Boolean,
    isApplyAll: Boolean = false,
    confirmAction: () -> Unit,
) {
    object : BaseDialog<DialogRepayAndReapplyBinding>(
        this,
        DialogRepayAndReapplyBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            cbUnderstand.isSelected = true
            if (isApplyAll) {
                cbUnderstand.setSelectedImageResource(R.mipmap.icon_repay_all_select)
            }
            tvTitle.isVisible = !isApplyAll
            messageLayout.isSelected = isApplyAll
            tvDesc.setText(
                if (isApplyAll) {
                    R.string.repay_auto_apply_all_dialog_desc
                } else {
                    R.string.repay_auto_apply_dialog_desc
                },
            )
            tvHint.text = this@showRepayAndReapplyDialog.createRepayHintText()
            btnApply.text = getString(
                if (isApplyAll) R.string.repay_auto_apply_all else R.string.repay_auto_apply
            )
            btnApply.updateAppearance(
                variant = StatefulActionButton.VARIANT_FILLED,
                solidColor = ContextCompat.getColor(
                    this@showRepayAndReapplyDialog,
                    when {
                        isApplyAll -> R.color.brand_secondary
                        isDue -> R.color.status_error
                        else -> R.color.brand_primary
                    },
                ),
            )
            cbUnderstand.singleClick {
                cbUnderstand.isSelected = !cbUnderstand.isSelected
            }
            tvUnderstand.singleClick {
                cbUnderstand.isSelected = !cbUnderstand.isSelected
            }
            btnApply.singleClick {
                if (!cbUnderstand.isSelected) {
                    getString(R.string.toast_repay_auto_apply_agreement).showToastMessage()
                    return@singleClick
                }
                dismiss()
                confirmAction()
            }
        }
    }.show()
}

private fun Context.createRepayHintText(): CharSequence {
    val hintText = getString(R.string.repay_auto_apply_dialog_hint)
    val repayText = getString(R.string.repay)
    val repayStart = hintText.lastIndexOf(repayText, ignoreCase = true)
    return SpannableString(hintText).apply {
        if (repayStart >= 0) {
            val repayEnd = repayStart + repayText.length
            setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(this@createRepayHintText, R.color.action_withdraw)
                ),
                repayStart,
                repayEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            setSpan(
                StyleSpan(Typeface.BOLD),
                repayStart,
                repayEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }
}

fun Context.createPaybackDialog(): Dialog {
    return object : BaseDialog<DialogNoActiveRepaymentBinding>(
        this,
        DialogNoActiveRepaymentBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            window?.attributes?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            root.setOnClickListener { dismiss() }
            tvBorrow.singleClick {
                dismiss()
                PortalHostActivity.launch(this@createPaybackDialog)
            }
        }
    }
}
