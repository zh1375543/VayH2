package com.velora.portal.core.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.google.android.play.core.review.ReviewManagerFactory
import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseDialog
import com.velora.portal.core.ui.base.BaseSheetDialog
import com.velora.portal.core.common.data.rateApp
import com.velora.portal.databinding.ConfirmDialogBinding
import com.velora.portal.databinding.DialogAppRatingBinding
import com.velora.portal.databinding.DialogAppUpdateBinding
import com.velora.portal.databinding.DialogBlockingProgressBinding
import com.velora.portal.databinding.DialogFeedbackBinding
import com.velora.portal.core.ui.extension.hideKeyboard
import com.velora.portal.core.ui.extension.setRoundedRectangleBackground
import com.velora.portal.core.ui.extension.setSpannableClickableText
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.APP_UPGRADE
import com.velora.portal.core.common.util.context.resolveColorCompat
import com.velora.portal.core.common.util.ExternalActionLauncher
import com.velora.portal.core.common.util.showToastMessage
import com.velora.portal.core.common.util.toHtmlSpanned
import com.velora.portal.core.common.util.trackEvent

fun Context.createLoadingDialog(message: String = getString(R.string.loading)): Dialog {
    return object : BaseDialog<DialogBlockingProgressBinding>(
        this@createLoadingDialog,
        DialogBlockingProgressBinding::inflate
    ) {
        override fun initView() = with(binding) {
            super.initView()
            tvMessage.text = message
        }
    }.apply {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}

fun Context.showConfirmDialog(
    title: String = "",
    desc: String = getString(R.string.sure_logout),
    cancel: String = getString(R.string.closed),
    ok: String = getString(R.string.sure),
    highLight: String = "XXXXXXXXXXX",
    cancelButtonSurfaceSecondary: Boolean = false,
    cancelAction: () -> Unit = {},
    okAction: () -> Unit,
) {
    object : BaseDialog<ConfirmDialogBinding>(
        this,
        ConfirmDialogBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            tvTitle.isVisible = title.isNotBlank()
            tvTitle.text = title
            tvDesc.setSpannableClickableText(
                desc,
                highLight,
                resolveColorCompat(R.color.text_primary)
            ) {}
            tvDesc.isVisible = desc.isNotBlank()
            tvSure.text = ok
            tvClose.text = cancel
            if (cancelButtonSurfaceSecondary) {
                val surfaceSecondary = resolveColorCompat(R.color.surface_secondary)
                tvClose.setRoundedRectangleBackground(
                    solidColor = surfaceSecondary,
                    radius = resources.getDimension(R.dimen.dp_16),
                    strokeColor = surfaceSecondary,
                    strokeWidth = resources.getDimension(R.dimen.dp_1),
                )
            }
            tvClose.singleClick {
                dismiss()
                cancelAction()
            }
            tvSure.singleClick {
                dismiss()
                okAction.invoke()
            }
        }
    }.show()
}

fun Context.createVersionUpdateDialog(): Dialog {
    return object : BaseDialog<DialogAppUpdateBinding>(
        this@createVersionUpdateDialog,
        DialogAppUpdateBinding::inflate
    ) {
        override fun initView() = with(binding) {
            super.initView()
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            tvOK.singleClick {
                trackEvent(APP_UPGRADE)
                ExternalActionLauncher.openStoreListing(
                    this@createVersionUpdateDialog,
                    "https://play.google.com/store/apps/details?id=$packageName",
                )
            }
        }
    }
}

fun Activity.showAppRatingDialog(action: (String) -> Unit) {
    if (rateApp) return
    rateApp = true
    object : BaseSheetDialog<DialogAppRatingBinding>(this, DialogAppRatingBinding::inflate) {
        override fun initView() = with(binding) {
            super.initView()
            tvRatingQuote.text = String.format(
                getString(R.string.rate_name),
                getString(R.string.app_name)
            ).toHtmlSpanned()
            ratingActionContainer.setOnClickListener {
                try {
                    val manager = ReviewManagerFactory.create(this@showAppRatingDialog)
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            val flow = manager.launchReviewFlow(this@showAppRatingDialog, reviewInfo)
                            flow.addOnCompleteListener {
                                // don't do any
                            }
                        } else {
                            Log.e("InAppReview", "request failed", task.exception)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TAG", e.message.toString())
                }
                dismiss()
            }
            btnRatingDecline.setOnClickListener {
                dismiss()
                showFeedbackDialog(action)
            }
        }
    }.show()
}

fun Activity.showFeedbackDialog(action: (String) -> Unit) {
    object : BaseDialog<DialogFeedbackBinding>(this, DialogFeedbackBinding::inflate) {
        override fun initView() = with(binding) {
            super.initView()
            window?.attributes?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.CENTER
            }
            root.setOnClickListener {
                etFeedbackMessage.hideKeyboard()
            }
            btnSubmitFeedback.setOnClickListener {
                if (etFeedbackMessage.text.isNullOrBlank()) {
                    getString(R.string.enter_feedback).showToastMessage()
                    return@setOnClickListener
                }
                action(etFeedbackMessage.text.toString())
                dismiss()
            }
        }
    }.show()
}
