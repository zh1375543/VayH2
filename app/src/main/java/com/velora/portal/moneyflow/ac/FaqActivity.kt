package com.velora.portal.moneyflow.ac

import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivityFaqBinding
import com.velora.portal.platform.common.util.viewBinding

/** Displays the fixed frequently asked questions for savings plans. */
class FaqActivity : BaseActivity<ActivityFaqBinding>() {

    override val binding by viewBinding(ActivityFaqBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
    }
}
