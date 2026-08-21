package com.velora.portal.journey.account.profile.presentation

import com.velora.portal.BuildConfig
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivityPreferencesBinding
import com.velora.portal.journey.communication.support.presentation.SupportFeedbackActivity
import com.velora.portal.platform.design.dialog.showConfirmDialog
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

class SetActivity : BaseActivity<ActivityPreferencesBinding>() {

    override val binding by viewBinding(ActivityPreferencesBinding::inflate)
    override fun initView() = with(binding) {
        tvVersion.text = BuildConfig.VERSION_NAME
        tvCloseAccount.singleClick {
            start<LogoutActivity>()
        }
        tvFeedback.singleClick {
            start<SupportFeedbackActivity>()
        }
        tvLogout.singleClick {
            showConfirmDialog {
                logOut()
            }
        }
    }
}
