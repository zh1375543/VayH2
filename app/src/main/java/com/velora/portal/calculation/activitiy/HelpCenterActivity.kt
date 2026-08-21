package com.velora.portal.calculation.activitiy

import android.content.Intent
import androidx.activity.viewModels
import com.velora.portal.BuildConfig
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.SidepageHelpCenterActivityBinding
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.journey.lending.dashboard.presentation.dialog.showContactUsDialog
import com.velora.portal.journey.lending.dashboard.presentation.VisitorPortalViewModel
import com.velora.portal.platform.common.util.viewBinding

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
