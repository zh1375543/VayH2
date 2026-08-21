package com.velora.portal.feature.profile.presentation

import com.velora.portal.BuildConfig
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.ActivityPreferencesBinding
import com.velora.portal.feature.support.presentation.SupportFeedbackActivity
import com.velora.portal.core.ui.dialog.showConfirmDialog
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.start
import com.velora.portal.core.common.util.viewBinding

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
