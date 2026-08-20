package com.novexa.platform.feature.onboarding.presentation

import androidx.lifecycle.lifecycleScope
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityVerificationCompleteBinding
import com.novexa.platform.app.MainActivity
import com.novexa.platform.core.common.util.countdownTimer
import com.novexa.platform.core.common.util.context.resolveColorCompat
import com.novexa.platform.core.ui.extension.setClickableTextWithScale
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.common.util.viewBinding

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
