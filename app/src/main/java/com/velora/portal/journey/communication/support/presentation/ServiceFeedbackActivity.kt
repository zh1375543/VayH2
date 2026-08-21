package com.velora.portal.journey.communication.support.presentation

import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenServiceFeedbackBinding
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.platform.showSoftInput
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.common.util.viewBinding

class ServiceFeedbackActivity : BaseActivity<ScreenServiceFeedbackBinding>() {

    override val binding by viewBinding(ScreenServiceFeedbackBinding::inflate)
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
