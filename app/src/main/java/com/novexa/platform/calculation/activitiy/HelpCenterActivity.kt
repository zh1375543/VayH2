package com.novexa.platform.calculation.activitiy

import android.content.Intent
import androidx.activity.viewModels
import com.novexa.platform.BuildConfig
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.SidepageHelpCenterActivityBinding
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.feature.dashboard.presentation.dialog.showContactUsDialog
import com.novexa.platform.feature.dashboard.presentation.VisitorPortalViewModel
import com.novexa.platform.core.common.util.viewBinding

/** Support landing page for the side-page experience. */
class HelpCenterActivity : BaseActivity<SidepageHelpCenterActivityBinding>() {

    override val binding by viewBinding(SidepageHelpCenterActivityBinding::inflate)
    private val vm by viewModels<VisitorPortalViewModel>()

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        tvAppName.text = getString(R.string.app_name)
        tvVersion.text = getString(R.string.version_value, BuildConfig.VERSION_NAME)

        tvContactUs.singleClick {
            vm.getUnAuthData(true)
        }
        tvFaq.singleClick {
            startActivity(Intent(this@HelpCenterActivity, FaqActivity::class.java))
        }

    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        result.observe(this@HelpCenterActivity) {
            it?.let(::showContactUsDialog)
        }
    }
}
