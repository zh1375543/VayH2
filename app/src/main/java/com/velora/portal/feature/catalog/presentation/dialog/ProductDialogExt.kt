package com.velora.portal.feature.catalog.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.view.isVisible
import com.velora.portal.BuildConfig
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseDialog
import com.velora.portal.platform.design.base.BaseSheetDialog
import com.velora.portal.platform.common.data.PRODUCT_AGREEMENT
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.feature.catalog.model.CatalogItemBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.databinding.DialogAvailableCreditBinding
import com.velora.portal.databinding.DialogLoanAgreementBinding
import com.velora.portal.databinding.DialogLoanOfferPickerBinding
import com.velora.portal.feature.content.presentation.ContentBrowserActivity
import com.velora.portal.feature.catalog.presentation.adapter.LoanOfferPickerAdapter
import com.velora.portal.platform.design.extension.singleClick

fun Context.showLoanAgreementDialog(
    isTogether: Boolean = false,
    productId: String? = null,
    amount: String? = null,
    applyAction: () -> Unit,
) {
    object :
        BaseSheetDialog<DialogLoanAgreementBinding>(
            this,
            DialogLoanAgreementBinding::inflate
        ) {
        override fun initView() = with(binding) {
            super.initView()
            tvDesc.text = String.format(
                getString(R.string.agreement_confirmation_desc),
                BuildConfig.HTTP_HOST
            )
            tvPlease.isVisible = !isTogether
            tvLease.isVisible = !isTogether
            btnApply.singleClick {
                dismiss()
                applyAction()
            }
            tvLease.singleClick {
                ContentBrowserActivity.launch(
                    this@showLoanAgreementDialog, tvLease.text.toString(),
                    PRODUCT_AGREEMENT + "userId=${SessionStore.loginInfo?.id}&productId=${productId}&amount=${amount}"
                )
            }
        }
    }.show()
}



fun Context.createNewProductDialog(
    list: List<CatalogItemBean>,
    closeAction: () -> Unit = {},
    action: () -> Unit,
): Dialog {
    return object : BaseDialog<DialogLoanOfferPickerBinding>(
        this,
        DialogLoanOfferPickerBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            window?.decorView?.setPadding(0, 0, 0, 0)
            var shouldTrackClose = true
            val productCount = list.size.toString()
            val fullText = String.format(getString(R.string.home_product_num), list.size)
            tvTitle.text = SpannableString(fullText).apply {
                val start = fullText.indexOf(productCount)
                if (start >= 0) {
                    setSpan(
                        ForegroundColorSpan(
                            this@createNewProductDialog.resolveColorCompat(R.color.brand_secondary)
                        ),
                        start,
                        start + productCount.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
            }
            rvProduct.adapter = LoanOfferPickerAdapter().apply {
                submitItems(list)
            }
            ivClose.singleClick {
                dismiss()
            }
            tvLoan.singleClick {
                shouldTrackClose = false
                dismiss()
                action.invoke()
            }
            setOnDismissListener {
                if (shouldTrackClose) closeAction.invoke()
            }
        }
    }
}

fun Context.createAvailableCreditDialog(
    amount: CharSequence,
    withdrawAction: () -> Unit,
): Dialog {
    return object : BaseDialog<DialogAvailableCreditBinding>(
        this,
        DialogAvailableCreditBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            tvAmount.text = amount
            tvLater.singleClick { dismiss() }
            btnWithdraw.singleClick {
                dismiss()
                withdrawAction()
            }
        }
    }
}
