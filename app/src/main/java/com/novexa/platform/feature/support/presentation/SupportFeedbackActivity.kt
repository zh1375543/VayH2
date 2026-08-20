package com.novexa.platform.feature.support.presentation

import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivitySupportFeedbackBinding
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.common.util.platform.showSoftInput
import com.novexa.platform.core.common.util.showToastMessage
import com.novexa.platform.core.common.util.viewBinding

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
