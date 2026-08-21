package com.velora.portal.feature.profile.presentation

import com.velora.portal.BuildConfig
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.ActivityAboutBinding
import com.velora.portal.core.common.util.viewBinding

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    override val binding by viewBinding(ActivityAboutBinding::inflate)

    override fun initView() {
        binding.tvVersion.text = BuildConfig.VERSION_NAME
    }
}
