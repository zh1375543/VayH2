package com.velora.portal.feature.support.presentation

import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.ActivitySupportFeedbackBinding
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.platform.showSoftInput
import com.velora.portal.core.common.util.showToastMessage
import com.velora.portal.core.common.util.viewBinding

class SupportFeedbackActivity : BaseActivity<ActivitySupportFeedbackBinding>() {

    override val binding by viewBinding(ActivitySupportFeedbackBinding::inflate)
    override fun initView() = with(binding) {
        etContent.requestFocus()
        showSoftInput(etContent)
        tvSubmit.singleClick {
            if (etContent.text.isNullOrBlank()) {
                getString(R.string.enter_feedback).showToastMessage()
                return@singleClick
            }
            getString(R.string.feedback_success).showToastMessage()
            finish()
        }
    }
}
