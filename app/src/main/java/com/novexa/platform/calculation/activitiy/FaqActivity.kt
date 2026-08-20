package com.novexa.platform.calculation.activitiy

import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.SidepageFaqActivityBinding
import com.novexa.platform.core.common.util.viewBinding

/** Displays the fixed frequently asked questions for savings plans. */
class FaqActivity : BaseActivity<SidepageFaqActivityBinding>() {

    override val binding by viewBinding(SidepageFaqActivityBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
    }
}
