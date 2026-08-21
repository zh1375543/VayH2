package com.velora.portal.feature.onboarding.presentation.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Spanned
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.location.LocationManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.velora.portal.app.MainApplication
import com.velora.portal.R
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.core.common.data.ACT_InputPhoneNumberEnd
import com.velora.portal.core.common.data.ACT_InputPhonenumberStart
import com.velora.portal.core.common.data.ACT_clickLoginOTP
import com.velora.portal.core.common.data.ACT_clickOTPLogin
import com.velora.portal.core.common.data.ACT_clickVerifyCode
import com.velora.portal.core.common.data.ACT_exit
import com.velora.portal.core.common.data.ACT_in
import com.velora.portal.core.common.data.PageExit
import com.velora.portal.core.common.data.PageHome
import com.velora.portal.core.common.data.PageLogin
import com.velora.portal.core.common.data.AGREEMENT_REGISTER
import com.velora.portal.core.common.data.PRIVACY_POLICY
import com.velora.portal.core.common.data.bean.ClickablePart
import com.velora.portal.core.common.data.bean.TrackBean
import com.velora.portal.core.common.data.location
import com.velora.portal.databinding.ActivityAccountAccessBinding
import com.velora.portal.feature.content.presentation.ContentBrowserActivity
import com.velora.portal.core.ui.extension.setSpannableClickableTexts
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.app.MainNavigator
import com.velora.portal.core.ui.dialog.showConfirmDialog
import com.velora.portal.calculation.CalculationActivity
import com.velora.portal.feature.dashboard.presentation.VisitorPortalViewModel
import com.velora.portal.core.common.util.LOGIN_VIA_OTP
import com.velora.portal.core.common.util.PermissionCoordinator
import com.velora.portal.core.common.util.countdownTimer
import com.velora.portal.core.common.util.context.resolveColorCompat
import com.velora.portal.core.common.util.text.isPhoneNumberValid
import com.velora.portal.core.common.util.showToastMessage
import com.velora.portal.core.common.util.trackEvent
import com.velora.portal.core.common.util.viewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class AccountAccessActivity : BaseActivity<ActivityAccountAccessBinding>() {

    override val binding by viewBinding(ActivityAccountAccessBinding::inflate)

    private val vm by viewModels<AccessSessionViewModel>()
    private val homeVm by viewModels<VisitorPortalViewModel>()

    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }

    private var startInputTime: Long = 0L
    private var debounceJob: Job? = null
    private val debounceTime = 500L  // treat as input finished after 500ms idle

    private var canNavigateBack = false
    private var lastBackPressTime = 0L

    private val smsHelper by lazy {
        SmsAutoFillHelper { code ->
            binding.formOtp.getEditText().apply {
                setText(code)
                setSelection(code.length)
            }
        }
    }

    override fun initView() {
        configureLoginHeader()
        initializeLoginSession()
        observePhoneNumberInput()
        bindLoginActions()
    }

    private fun configureLoginHeader() = with(binding) {
        accessPageHeader.showNavigation(false)
        accessPageHeader.setNavigationAction { handleLoginBack() }
        tvAccessHeadline.text = getString(
            R.string.welcome_to_app,
            getString(R.string.app_name)
        )
        cbAccept.isSelected = true
    }

    private fun initializeLoginSession() {
        smsHelper.register(this@AccountAccessActivity)
        vm.submitTrackingEvent(
            TrackBean(
                p = PageLogin,
                act = ACT_in
            )
        )
        onBackPressedDispatcher.addCallback(
            this@AccountAccessActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = handleLoginBack()
            },
        )
        homeVm.getUnAuthData()
    }

    private fun observePhoneNumberInput() = with(binding) {
        formPhone.getEditText().doOnTextChanged { _, _, _, _ ->
            val now = System.currentTimeMillis()

            // 1. first input → record start time
            if (startInputTime == 0L) {
                startInputTime = now
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageLogin,
                        act = ACT_InputPhonenumberStart,
                        result = startInputTime.toString()
                    )
                )
            }

            // 2. typing → reset end timer
            debounceJob?.cancel()
            debounceJob = lifecycleScope.launch {
                delay(debounceTime.milliseconds)

                // 3. user stopped typing → record end time

                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageLogin,
                        act = ACT_InputPhoneNumberEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun bindLoginActions() = with(binding) {
        tvAccessHeadline.singleClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageLogin,
                    act = ACT_clickOTPLogin,
                )
            )
        }
        tvAccept.setSpannableClickableTexts(
            String.format(
                getString(R.string.accept_policy),
                getString(R.string.privacy_agreement),
                getString(R.string.privacy_blue)
            ),
            arrayListOf(
                ClickablePart(
                    getString(R.string.privacy_agreement),
                    resolveColorCompat(R.color.brand_primary),
                    onClick = {
                        ContentBrowserActivity.Companion.launch(
                            this@AccountAccessActivity,
                            getString(R.string.privacy_agreement),
                            AGREEMENT_REGISTER
                        )
                    }),
                ClickablePart(
                    getString(R.string.privacy_blue),
                    resolveColorCompat(R.color.brand_primary),
                    onClick = {
                        ContentBrowserActivity.Companion.launch(
                            this@AccountAccessActivity,
                            getString(R.string.privacy_blue),
                            PRIVACY_POLICY
                        )
                    }),
            )
        )
        cbAccept.singleClick {
            cbAccept.isSelected = !cbAccept.isSelected
        }
        tvResendOtp.singleClick {
            if (tvResendOtp.isEnabled) requestVerificationCode()
        }
        tvLogin.singleClick {
            if (!cbAccept.isSelected) {
                getString(R.string.privacy_toast_agree2).showToastMessage()
                return@singleClick
            }
            if (formPhone.isVisible) requestVerificationCode() else submitLogin()
        }
        if (location.first == 0.0
            && PermissionCoordinator.hasPermission(
                this@AccountAccessActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        ) {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                location = it.longitude to it.latitude
            }
        }
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        otpResult.observe(this@AccountAccessActivity) {
            smsHelper.startListening()
            showOtpInput()
        }
        loginResult.observe(this@AccountAccessActivity) {
            it?.let {
                MainApplication.Companion.appViewModel.postRiskInfo(PageLogin) {}
                vm.postDeviceInfo()
                launchPostLoginDestination()
//                CalculationActivity.launch(this@LoginActivity)
                finish()
            }
        }
        homeVm.result.observe(this@AccountAccessActivity) {
            canNavigateBack = it?.showBackButton?.trim() == "1"
            binding.accessPageHeader.showNavigation(canNavigateBack)
        }
    }

    private fun handleLoginBack() {
        if (canNavigateBack) {
            showConfirmDialog(
                title = getString(R.string.login_prompt_title),
                desc = getString(R.string.login_prompt_description),
                cancel = getString(R.string.credit_dialog_later),
                ok = getString(R.string.login_now),
                cancelAction = {
                    CalculationActivity.launch(this)
                    finish()
                },
                okAction = {},
            )
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < EXIT_INTERVAL) {
            vm.submitTrackingEvent(
                TrackBean(
                    act = ACT_exit,
                    result = PageHome,
                    p = PageExit,
                )
            )
            finishAffinity()
        } else {
            lastBackPressTime = currentTime
            getString(R.string.again_exit).showToastMessage()
        }
    }

    private fun launchPostLoginDestination() {
        MainNavigator.launch(this, clearTask = true)
    }

    private fun requestVerificationCode() {
        val phoneNumber = binding.formPhone.getText()
        when {
            phoneNumber.isBlank() -> {
                binding.formPhone.showError(getString(R.string.phone_number_required))
                return
            }

            !phoneNumber.isPhoneNumberValid() -> {
                binding.formPhone.showError(getString(R.string.invalid_phone_number_format))
                return
            }
        }
        sendCode(phoneNumber)
    }

    private fun sendCode(phoneNumber: String) {
        vm.submitTrackingEvent(
            TrackBean(
                p = PageLogin,
                act = ACT_clickVerifyCode
            )
        )
        vm.sendOTP(phoneNumber)
    }

    private fun showOtpInput() = with(binding) {
        formPhone.isVisible = false
        formOtp.isVisible = true
        tvAccessHeadline.isVisible = false
        loginPhoneHint.isVisible = false
        showOtpDescription()
        tvLogin.text = getString(R.string.login)
        startResendCountdown()
        formOtp.getEditText().requestFocus()
    }

    private fun showOtpDescription() = with(binding) {
        val phoneNumber = "+63 ${formPhone.getText()}"
        val message = getString(R.string.verification_code_sent, phoneNumber)
        tvOtpDescription.isVisible = true
        tvOtpDescription.text = SpannableString(message).apply {
            val start = message.indexOf(phoneNumber)
            setSpan(
                ForegroundColorSpan(resolveColorCompat(R.color.badge_promotion)),
                start,
                start + phoneNumber.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun startResendCountdown() = with(binding) {
        tvResendOtp.isVisible = true
        tvResendOtp.isEnabled = false
        tvResendOtp.setTextColor(resolveColorCompat(R.color.text_secondary))
        updateResendCountdown(59)
        lifecycleScope.countdownTimer(
            58,
            {},
            end = {
                tvResendOtp.text = getString(R.string.resend_verification_code)
                tvResendOtp.setTextColor(resolveColorCompat(R.color.brand_secondary))
                tvResendOtp.isEnabled = true
            },
        ) { seconds ->
            updateResendCountdown(seconds)
        }
    }

    private fun updateResendCountdown(seconds: Long) = with(binding) {
        val secondsText = seconds.toString()
        val message = getString(R.string.resend_after, secondsText)
        tvResendOtp.text = SpannableString(message).apply {
            val start = message.indexOf(secondsText)
            setSpan(
                ForegroundColorSpan(resolveColorCompat(R.color.brand_secondary)),
                start,
                start + secondsText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun submitLogin() {
        val otp = binding.formOtp.getText()
        if (otp.isBlank()) {
            binding.formOtp.showError(getString(R.string.verification_code_required))
            return
        }

        trackEvent(LOGIN_VIA_OTP)
        vm.submitTrackingEvent(
            TrackBean(
                p = PageLogin,
                act = ACT_clickLoginOTP,
            ),
        )
        vm.login(binding.formPhone.getText(), otp, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        smsHelper.unregister()
    }

    companion object {
        const val EXTRA_RETURN_TO_PORTAL = "return_to_portal"

        fun launchForPortal(context: Context) {
            context.startActivity(
                Intent(context, AccountAccessActivity::class.java)
                    .putExtra(EXTRA_RETURN_TO_PORTAL, true),
            )
        }

        const val EXIT_INTERVAL = 2_000L
    }
}
