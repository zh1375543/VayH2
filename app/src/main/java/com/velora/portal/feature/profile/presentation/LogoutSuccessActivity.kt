package com.velora.portal.feature.profile.presentation

import androidx.lifecycle.lifecycleScope
import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.ActivityLogoutCompleteBinding
import com.velora.portal.core.common.util.countdownTimer
import com.velora.portal.core.common.util.context.resolveColorCompat
import com.velora.portal.core.ui.extension.setClickableTextWithScale
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.viewBinding

class LogoutSuccessActivity :
    BaseActivity<ActivityLogoutCompleteBinding>() {

    override val binding by viewBinding(ActivityLogoutCompleteBinding::inflate)
    private val returnToPortal by lazy {
        intent.getBooleanExtra(EXTRA_RETURN_TO_PORTAL, false)
    }

    override fun initView() = with(binding) {
        applyTopInset(root)
        tvOK.singleClick {
            handleBackPressed()
        }
        registerTrackedBackHandler(null) {
            handleBackPressed()
        }
        lifecycleScope.countdownTimer(10, next = { seconds ->
            binding.tvTips.setClickableTextWithScale(
                String.format(getString(R.string.back_to_home_tips), seconds.toString()),
                seconds.toString(),
                resolveColorCompat(R.color.action_withdraw)
            )
        }, end = {
            handleBackPressed()
        })
        tvTips.setClickableTextWithScale(
            String.format(getString(R.string.back_to_home_tips), "10"),
            "10",
            resolveColorCompat(R.color.action_withdraw)
        )
    }

    private fun handleBackPressed() {
        logOut(true)
    }

    companion object {
        const val EXTRA_RETURN_TO_PORTAL = "return_to_portal"
    }
}
