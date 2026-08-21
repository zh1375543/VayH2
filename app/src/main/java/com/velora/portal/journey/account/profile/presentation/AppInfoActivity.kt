package com.velora.portal.journey.account.profile.presentation

import com.velora.portal.BuildConfig
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenAppInfoBinding
import com.velora.portal.platform.common.util.viewBinding

class AppInfoActivity : BaseActivity<ScreenAppInfoBinding>() {

    override val binding by viewBinding(ScreenAppInfoBinding::inflate)

    override fun initView() {
        binding.tvVersion.text = BuildConfig.VERSION_NAME
    }
}
