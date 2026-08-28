package com.velora.portal.journey.access.presentation.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.LocationManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.velora.portal.application.MainApplication
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.ACT_InputPhoneNumberEnd
import com.velora.portal.platform.common.data.ACT_InputPhonenumberStart
import com.velora.portal.platform.common.data.ACT_clickLoginOTP
import com.velora.portal.platform.common.data.ACT_clickOTPLogin
import com.velora.portal.platform.common.data.ACT_clickVerifyCode
import com.velora.portal.platform.common.data.ACT_exit
import com.velora.portal.platform.common.data.ACT_in
import com.velora.portal.platform.common.data.PageExit
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.PageLogin
import com.velora.portal.platform.common.data.AGREEMENT_REGISTER
import com.velora.portal.platform.common.data.PRIVACY_POLICY
import com.velora.portal.platform.common.data.bean.ClickablePart
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.common.data.location
import com.velora.portal.databinding.ScreenPhoneAuthBinding
import com.velora.portal.platform.browser.presentation.ContentBrowserActivity
import com.velora.portal.platform.design.extension.setSpannableClickableTexts
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.application.MainNavigator
import com.velora.portal.platform.design.dialog.showConfirmDialog
import com.velora.portal.moneyflow.PayPilotActivity
import com.velora.portal.journey.access.presentation.authenticate.AccessSessionViewModel
import com.velora.portal.journey.access.presentation.authenticate.SmsAutoFillHelper
import com.velora.portal.journey.lending.dashboard.presentation.VisitorPortalViewModel
import com.velora.portal.platform.common.util.LOGIN_VIA_OTP
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.countdownTimer
import com.velora.portal.platform.common.util.context.resolveColorCompat
import com.velora.portal.platform.common.util.text.isPhoneNumberValid
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.platform.common.util.viewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PhoneAuthActivity : BaseActivity<ScreenPhoneAuthBinding>() {

    override val binding by viewBinding(ScreenPhoneAuthBinding::inflate)

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
        setupAccessScreen()
        startAccessSession()
        trackPhoneInput()
        bindAccessEvents()
    }

    private fun setupAccessScreen() = with(binding) {
        setLightSystemBarIcons(enabled = true)
        accessPageHeader.showNavigation(false)
        accessPageHeader.setNavigationAction { processAccessBack() }
        tvAccessHeadline.text = getString(
            R.string.welcome_to_app,
            getString(R.string.app_name)
        )
        loginPhoneHint.text = SpannableString(
            getString(R.string.login_invalid_phone_hint),
        ).apply {
            val suffix = "modify it."
            val start = lastIndexOf(suffix)
            if (start >= 0) {
                setSpan(
                    ForegroundColorSpan(resolveColorCompat(R.color.brand_secondary)),
                    start,
                    start + suffix.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                setSpan(
                    RelativeSizeSpan(1.17f),
                    start,
                    start + suffix.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + suffix.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
        cbAccept.isSelected = true
    }

    private fun startAccessSession() {
        smsHelper.register(this@PhoneAuthActivity)
        vm.submitTrackingEvent(
            TrackBean(
                p = PageLogin,
                act = ACT_in
            )
        )
        onBackPressedDispatcher.addCallback(
            this@PhoneAuthActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = processAccessBack()
            },
        )
        homeVm.getUnAuthData()
    }

    private fun trackPhoneInput() = with(binding) {
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
    private fun bindAccessEvents() = with(binding) {
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
                        ContentBrowserActivity.launch(
                            this@PhoneAuthActivity,
                            getString(R.string.privacy_agreement),
                            AGREEMENT_REGISTER
                        )
                    }),
                ClickablePart(
                    getString(R.string.privacy_blue),
                    resolveColorCompat(R.color.brand_primary),
                    onClick = {
                        ContentBrowserActivity.launch(
                            this@PhoneAuthActivity,
                            getString(R.string.privacy_blue),
                            PRIVACY_POLICY
                        )
                    }),
            )
        )
        cbAccept.singleClick {
            cbAccept.isSelected = !cbAccept.isSelected
        }
        tvGetOtp.singleClick {
            if (tvGetOtp.isEnabled) requestOtpCode()
        }
        tvLogin.singleClick {
            if (!cbAccept.isSelected) {
                getString(R.string.privacy_toast_agree2).showToastMessage()
                return@singleClick
            }
            performOtpLogin()
        }
        if (location.first == 0.0
            && PermissionCoordinator.hasPermission(
                this@PhoneAuthActivity,
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
        otpResult.observe(this@PhoneAuthActivity) {
            smsHelper.startListening()
            showOtpInput()
        }
        loginResult.observe(this@PhoneAuthActivity) {
            it?.let {
                MainApplication.appViewModel.postRiskInfo(PageLogin) {}
                vm.recordDeviceSnapshot()
               launchPostLoginDestination()
               // PayPilotActivity.launch(this@PhoneAuthActivity)
                finish()
            }
        }
        homeVm.result.observe(this@PhoneAuthActivity) {
            canNavigateBack = it?.showBackButton?.trim() == "1"
            binding.accessPageHeader.showNavigation(canNavigateBack)
        }
    }

    private fun processAccessBack() {
        if (canNavigateBack) {
            showConfirmDialog(
                title = getString(R.string.login_prompt_title),
                desc = getString(R.string.login_prompt_description),
                cancel = getString(R.string.credit_dialog_later),
                ok = getString(R.string.login_now),
                cancelAction = {
                    PayPilotActivity.launch(this)
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

    private fun requestOtpCode() {
        val phoneNumber = binding.formPhone.getText()
        when {
            phoneNumber.isBlank() -> {
                binding.formPhone.showError(getString(R.string.phone_number_required))
                getString(R.string.phone_number_required).showToastMessage()
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
        vm.requestOtpCode(phoneNumber)
    }

    private fun showOtpInput() = with(binding) {
        startResendCountdown()
        formOtp.getEditText().requestFocus()
    }

    private fun startResendCountdown() = with(binding) {
        tvGetOtp.isEnabled = false
        updateResendCountdown(59)
        lifecycleScope.countdownTimer(
            58,
            {},
            end = {
                tvGetOtp.text = getString(R.string.get_otp)
                tvGetOtp.isEnabled = true
            },
        ) { seconds ->
            updateResendCountdown(seconds)
        }
    }

    private fun updateResendCountdown(seconds: Long) = with(binding) {
        tvGetOtp.text = getString(R.string.resend_after_short, seconds)
    }

    private fun performOtpLogin() {
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
        vm.authenticate(phoneNumber, otp, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        smsHelper.unregister()
    }

    companion object {
        const val EXTRA_RETURN_TO_PORTAL = "return_to_portal"

        fun launchForPortal(context: Context) {
            context.startActivity(
                Intent(context, PhoneAuthActivity::class.java)
                    .putExtra(EXTRA_RETURN_TO_PORTAL, true),
            )
        }

        const val EXIT_INTERVAL = 2_000L
    }
}
