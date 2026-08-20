package com.novexa.platform.calculation.activitiy

import androidx.activity.viewModels
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.databinding.SidepageAccountDeletionActivityBinding
import com.novexa.platform.feature.profile.presentation.LogoutSuccessActivity
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.feature.onboarding.presentation.login.AccessSessionViewModel
import com.novexa.platform.core.common.util.maskPhoneNumber
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding

/** Confirms permanent account deletion for the side-page experience. */
class AccountDeletionActivity : BaseActivity<SidepageAccountDeletionActivityBinding>() {

    override val binding by viewBinding(SidepageAccountDeletionActivityBinding::inflate)
    private val viewModel by viewModels<AccessSessionViewModel>()

    override fun initView() = with(binding) {
        applyTopInset(root)
        titleBar.setNavigationAction(::finish)
        tvAppName.text = getString(R.string.app_name)
        tvAccountNumber.text = SessionStore.loginInfo?.phone.orEmpty().maskPhoneNumber()

        tvConfirm.singleClick {
            viewModel.logout()
        }
    }

    override fun initObserve() = with(viewModel) {
        logoutResult.observe(this@AccountDeletionActivity) {
            start<LogoutSuccessActivity> {
                putExtra(LogoutSuccessActivity.EXTRA_RETURN_TO_PORTAL, true)
            }
            finish()
        }
    }
}
