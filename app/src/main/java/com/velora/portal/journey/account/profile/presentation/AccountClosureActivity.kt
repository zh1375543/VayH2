package com.velora.portal.journey.account.profile.presentation

import androidx.activity.viewModels
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenAccountClosureBinding
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.journey.access.presentation.login.AccessSessionViewModel
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

class AccountClosureActivity : BaseActivity<ScreenAccountClosureBinding>() {

    override val binding by viewBinding(ScreenAccountClosureBinding::inflate)
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
        logoutResult.observe(this@AccountClosureActivity) {
            start<AccountClosureCompleteActivity>()
            finish()
        }
    }
}
