package com.novexa.platform.calculation.activitiy

import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.SidepageAccountSettingsActivityBinding
import com.novexa.platform.feature.onboarding.presentation.login.AccountAccessActivity
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.ui.dialog.showConfirmDialog
import com.novexa.platform.core.common.util.SPUtil
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding

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
