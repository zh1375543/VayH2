package com.velora.portal.app

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.velora.portal.feature.onboarding.presentation.login.AccountAccessActivity
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.core.common.data.ACT_inApp
import com.velora.portal.core.analytics.AnalyticsTracker
import com.velora.portal.core.common.data.bean.TrackBean
import com.velora.portal.core.session.SessionStore
import com.velora.portal.core.common.data.PageHome
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

import com.velora.portal.core.common.util.viewBinding
import com.velora.portal.databinding.ActivityStartupBinding

class LaunchActivity :
    BaseActivity<ActivityStartupBinding>() {

    override val binding by viewBinding(ActivityStartupBinding::inflate)

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
        secretRequestResult.observe(this@LaunchActivity) { completedRequestId ->
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
            MainNavigator.launch(this@LaunchActivity, clearTask = true)
//           CalculationActivity.launch(this)
        } else {
            startActivity(
                Intent(this@LaunchActivity, AccountAccessActivity::class.java).apply {
                    putExtra(AccountAccessActivity.EXTRA_RETURN_TO_PORTAL, true)
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
