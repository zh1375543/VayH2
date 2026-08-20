package com.novexa.platform.feature.profile.presentation

import androidx.activity.viewModels
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivitySignOutConfirmationBinding
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.feature.onboarding.presentation.login.AccessSessionViewModel
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding

class LogoutActivity : BaseActivity<ActivitySignOutConfirmationBinding>() {

    override val binding by viewBinding(ActivitySignOutConfirmationBinding::inflate)
    private val vm by viewModels<AccessSessionViewModel>()

    private val selectTextList by lazy {
        arrayListOf(
            binding.tvReason1,
            binding.tvReason2,
            binding.tvReason3,
            binding.tvReason4,
        )
    }

    override fun initView() = with(binding) {
        tvAccount.text = SessionStore.loginInfo?.phone
        btnSubmit.isEnabled = false
        selectTextList.forEach {
            it.setOnClickListener { it1 ->
                it1.isSelected = !it.isSelected
                btnSubmit.isEnabled = selectTextList.any { it1 -> it1.isSelected }
            }
        }
        btnSubmit.singleClick {
            vm.logout()
        }
    }

    override fun initObserve() =with(vm){
        super.initObserve()
        logoutResult.observe(this@LogoutActivity) {
            start<LogoutSuccessActivity>()
            finish()
        }
    }
}
