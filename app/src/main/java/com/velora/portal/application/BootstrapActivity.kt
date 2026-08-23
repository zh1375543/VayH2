package com.velora.portal.application

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.velora.portal.journey.access.presentation.login.PhoneAuthActivity
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.ACT_inApp
import com.velora.portal.platform.telemetry.analytics.AnalyticsTracker
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.platform.common.data.PageHome
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.databinding.ScreenBootstrapBinding
import com.velora.portal.moneyflow.PayPilotActivity

class BootstrapActivity :
    BaseActivity<ScreenBootstrapBinding>() {

    override val binding by viewBinding(ScreenBootstrapBinding::inflate)

    private var hasJump = false
    private var timeoutJob: Job? = null
    private var successJumpJob: Job? = null
    private var secretRequestId = 0L
    private var splashStartedAtMillis = 0L

    override fun initView() = with(binding) {
        applySplashSystemBars()
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return@with
        }
        splashStartedAtMillis = SystemClock.elapsedRealtime()
        MainApplication.appViewModel.apply {
            secretRequestId = getAppSecret()
            hasDeviceInfo(PageHome) {}
            AnalyticsTracker.startSession()
            submitTrackingEvent(
                TrackBean(
                    act = ACT_inApp
                )
            )
        }
        // Do not block startup forever if the request never completes.
        timeoutJob = lifecycleScope.launch {
            delay(MAXIMUM_WAIT_MILLIS.milliseconds)
            jumpNext()
        }
    }

    override fun initObserve() = with(MainApplication.appViewModel) {
        super.initObserve()
        secretRequestResult.observe(this@BootstrapActivity) { completedRequestId ->
            if (completedRequestId != secretRequestId) return@observe
            if (successJumpJob?.isActive == true) return@observe

            val elapsedMillis = SystemClock.elapsedRealtime() - splashStartedAtMillis
            val remainingMillis = (MINIMUM_DISPLAY_MILLIS - elapsedMillis).coerceAtLeast(0L)
            successJumpJob = lifecycleScope.launch {
                delay(remainingMillis.milliseconds)
                jumpNext()
            }
        }
    }

    private fun jumpNext() {
        if (hasJump) return
        hasJump = true
        if (SessionStore.isLoggedIn) {
//            MainNavigator.launch(this@BootstrapActivity, clearTask = true)
            PayPilotActivity.launch(this)
        } else {
            startActivity(
                Intent(this@BootstrapActivity, PhoneAuthActivity::class.java).apply {
                    putExtra(PhoneAuthActivity.EXTRA_RETURN_TO_PORTAL, true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }

    }

    private fun applySplashSystemBars() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }
    }

    private companion object {
        const val MINIMUM_DISPLAY_MILLIS = 1_500L
        const val MAXIMUM_WAIT_MILLIS = 10_000L
    }
}
