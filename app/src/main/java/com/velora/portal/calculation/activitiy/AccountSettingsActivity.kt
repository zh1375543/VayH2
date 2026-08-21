package com.velora.portal.calculation.activitiy

import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.SidepageAccountSettingsActivityBinding
import com.velora.portal.journey.access.presentation.authenticate.PhoneAuthActivity
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.design.dialog.showConfirmDialog
import com.velora.portal.platform.common.util.SPUtil
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

/** Settings entry point dedicated to the side-page experience. */
class AccountSettingsActivity : BaseActivity<SidepageAccountSettingsActivityBinding>() {

    override val binding by viewBinding(SidepageAccountSettingsActivityBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)

        tvDeleteAccount.singleClick {
            start<AccountDeletionActivity>()
        }
        tvLogOut.singleClick {
            showConfirmDialog {
                SPUtil.getInstance().clear()
                PhoneAuthActivity.launchForPortal(this@AccountSettingsActivity)
                finish()
            }
        }
    }
}
