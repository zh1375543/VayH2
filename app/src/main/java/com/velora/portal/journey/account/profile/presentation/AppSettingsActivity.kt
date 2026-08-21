package com.velora.portal.journey.account.profile.presentation

import com.velora.portal.BuildConfig
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenAppSettingsBinding
import com.velora.portal.journey.communication.support.presentation.ServiceFeedbackActivity
import com.velora.portal.platform.design.dialog.showConfirmDialog
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

class AppSettingsActivity : BaseActivity<ScreenAppSettingsBinding>() {

    override val binding by viewBinding(ScreenAppSettingsBinding::inflate)
    override fun initView() = with(binding) {
        tvVersion.text = BuildConfig.VERSION_NAME
        tvCloseAccount.singleClick {
            start<AccountClosureActivity>()
        }
        tvFeedback.singleClick {
            start<ServiceFeedbackActivity>()
        }
        tvLogout.singleClick {
            showConfirmDialog {
                logOut()
            }
        }
    }
}
