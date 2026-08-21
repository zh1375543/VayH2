package com.velora.portal.calculation.activitiy

import androidx.activity.viewModels
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.databinding.SidepageAccountDeletionActivityBinding
import com.velora.portal.journey.account.profile.presentation.AccountClosureCompleteActivity
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.journey.access.presentation.authenticate.AccessSessionViewModel
import com.velora.portal.platform.common.util.maskPhoneNumber
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

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
            viewModel.signOut()
        }
    }

    override fun initObserve() = with(viewModel) {
        logoutResult.observe(this@AccountDeletionActivity) {
            start<AccountClosureCompleteActivity> {
                putExtra(AccountClosureCompleteActivity.EXTRA_RETURN_TO_PORTAL, true)
            }
            finish()
        }
    }
}
