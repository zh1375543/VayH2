package com.novexa.platform.feature.profile.presentation

import com.novexa.platform.BuildConfig
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityPreferencesBinding
import com.novexa.platform.feature.support.presentation.SupportFeedbackActivity
import com.novexa.platform.core.ui.dialog.showConfirmDialog
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding

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
