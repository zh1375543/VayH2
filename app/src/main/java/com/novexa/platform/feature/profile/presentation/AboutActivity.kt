package com.novexa.platform.feature.profile.presentation

import com.novexa.platform.BuildConfig
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityAboutBinding
import com.novexa.platform.core.common.util.viewBinding

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    override val binding by viewBinding(ActivityAboutBinding::inflate)

    override fun initView() {
        binding.tvVersion.text = BuildConfig.VERSION_NAME
    }
}
