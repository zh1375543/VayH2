package com.velora.portal.journey.account.profile.presentation

import com.velora.portal.BuildConfig
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivityAboutBinding
import com.velora.portal.platform.common.util.viewBinding

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    override val binding by viewBinding(ActivityAboutBinding::inflate)

    override fun initView() {
        binding.tvVersion.text = BuildConfig.VERSION_NAME
    }
}
