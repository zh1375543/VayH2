package com.velora.portal.feature.onboarding.presentation

import androidx.lifecycle.lifecycleScope
import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.ActivityVerificationCompleteBinding
import com.velora.portal.application.MainActivity
import com.velora.portal.core.common.util.countdownTimer
import com.velora.portal.core.common.util.context.resolveColorCompat
import com.velora.portal.core.ui.extension.setClickableTextWithScale
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.viewBinding

class ReviewSuccessActivity :
    BaseActivity<ActivityVerificationCompleteBinding>() {

    override val binding by viewBinding(ActivityVerificationCompleteBinding::inflate)

    override fun initView() {
        configureSystemBarAndBack()
        configureCountdownAndConfirm()
    }

    /** Sets the light system bar and routes the hardware back action back home. */
    private fun configureSystemBarAndBack() = with(binding) {
        setLightSystemBarIcons(enabled = true)
        registerTrackedBackHandler(null) {
            finish()
            MainActivity.launch(this@ReviewSuccessActivity, isFromAuth = true)
        }
    }

    /** Drives the countdown tip text and wires up the confirm button to go back home. */
    private fun configureCountdownAndConfirm() = with(binding) {
        tvTips.setClickableTextWithScale(
            String.format(getString(R.string.back_to_home_tips), "10"),
            "10",
            resolveColorCompat(R.color.brand_primary)
        )
        lifecycleScope.countdownTimer(10, next = { seconds ->
            tvTips.setClickableTextWithScale(
                String.format(getString(R.string.back_to_home_tips), seconds.toString()),
                seconds.toString(),
                resolveColorCompat(R.color.brand_primary)
            )
        }, end = {
            tvOK.performClick()
        })
        tvOK.singleClick {
            finish()
            MainActivity.launch(this@ReviewSuccessActivity, isFromAuth = true)
        }
    }
}
