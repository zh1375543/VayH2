package com.velora.portal.calculation.activitiy

import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.SidepageAccountSettingsActivityBinding
import com.velora.portal.feature.onboarding.presentation.login.AccountAccessActivity
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.ui.dialog.showConfirmDialog
import com.velora.portal.core.common.util.SPUtil
import com.velora.portal.core.common.util.start
import com.velora.portal.core.common.util.viewBinding

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
                AccountAccessActivity.launchForPortal(this@AccountSettingsActivity)
                finish()
            }
        }
    }
}
