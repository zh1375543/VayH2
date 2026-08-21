package com.velora.portal.feature.support.presentation

import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivitySupportFeedbackBinding
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.platform.showSoftInput
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.common.util.viewBinding

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
