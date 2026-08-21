package com.velora.portal.feature.profile.presentation

import androidx.activity.viewModels
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.ActivitySignOutConfirmationBinding
import com.velora.portal.core.session.SessionStore
import com.velora.portal.feature.onboarding.presentation.login.AccessSessionViewModel
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.start
import com.velora.portal.core.common.util.viewBinding

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
