package com.velora.portal.journey.account.profile.presentation

import androidx.lifecycle.lifecycleScope
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenAccountClosureCompleteBinding
import com.velora.portal.platform.common.util.countdownTimer
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.platform.design.extension.setClickableTextWithScale
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.viewBinding

class AccountClosureCompleteActivity :
    BaseActivity<ScreenAccountClosureCompleteBinding>() {

    override val binding by viewBinding(ScreenAccountClosureCompleteBinding::inflate)
    private val returnToPortal by lazy {
        intent.getBooleanExtra(EXTRA_RETURN_TO_PORTAL, false)
    }

    override fun initView() = with(binding) {
        setStatusBarAppearance(
            statusBarColor = R.color.action_withdraw,
            useDarkStatusBarIcons = false,
        )
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
