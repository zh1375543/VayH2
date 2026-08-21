package com.velora.portal.calculation.activitiy

import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.SidepageFaqActivityBinding
import com.velora.portal.platform.common.util.viewBinding

/** Displays the fixed frequently asked questions for savings plans. */
class FaqActivity : BaseActivity<SidepageFaqActivityBinding>() {

    override val binding by viewBinding(SidepageFaqActivityBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
    }
}
