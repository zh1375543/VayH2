package com.novexa.platform.feature.onboarding.presentation.login

import androidx.lifecycle.MutableLiveData
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.core.common.data.ACT_OTPFail
import com.novexa.platform.core.common.data.ACT_createPassword
import com.novexa.platform.core.common.data.ACT_getVerifyCode
import com.novexa.platform.core.common.data.ACT_loginOTP
import com.novexa.platform.core.common.data.ACT_loginPassword
import com.novexa.platform.core.common.data.PageCreatePassword
import com.novexa.platform.core.common.data.PageLogin
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.feature.onboarding.data.SessionRepository
import com.novexa.platform.feature.onboarding.model.AccessSessionResponse
import com.novexa.platform.core.common.util.text.toJsonString

class AccessSessionViewModel(
    private val sessionRepository: SessionRepository = SessionRepository(),
) : BaseViewModel() {

    val otpResult = MutableLiveData<Any?>()
    fun sendOTP(phone: String) {
        createNetworkRequest { sessionRepository.sendOTP(phone) }
            .showLoading().onSuccess {
                submitTrackingEvent(
                    TrackBean(
                        p = PageLogin,
                        act = ACT_getVerifyCode,
                        result = it.toJsonString()
                    )
                )
                otpResult.value = it
            }.onFailed {
                submitTrackingEvent(
                    TrackBean(
                        p = PageLogin,
                        act = ACT_getVerifyCode,
                        result = it.toJsonString()
                    )
                )
                submitTrackingEvent(
                    TrackBean(
                        p = PageLogin,
                        act = ACT_OTPFail,
                        result = it.toJsonString()
                    )
                )
                false
            }
    }

    val loginResult = MutableLiveData<AccessSessionResponse?>()
    fun login(
        phone: String,
        code: String?,
        password: String?,
    ) {
        createNetworkRequest {
            sessionRepository.login(phone, code, password)
        }.showLoading().onSuccess {
            it?.let {
                submitTrackingEvent(
                    TrackBean(
                        p = PageLogin,
                        act = if (password == null) ACT_loginOTP else ACT_loginPassword,
                        result = it.toJsonString()
                    )
                )
                SessionStore.token = it.token
                SessionStore.activityUrl = it.activityUrl.orEmpty()
                SessionStore.loginInfo = it
                loginResult.value = it
            }
        }.onFailed {
            submitTrackingEvent(
                TrackBean(
                    p = PageLogin,
                    act = if (password == null) ACT_loginOTP else ACT_loginPassword,
                    result = it.toJsonString()
                )
            )
            false
        }
    }

    fun postDeviceInfo() {
        createNetworkRequest {
            sessionRepository.postDeviceInfo()
        }.onSuccess { }.execute()
    }

    val logoutResult = MutableLiveData<Any?>()
    fun logout() {
        createNetworkRequest {
            sessionRepository.logout()
        }.showLoading().onSuccess {
            logoutResult.value = it
        }.execute()
    }

    val sendChangePasswordOtpResult = MutableLiveData<Any?>()
    fun sendChangePasswordOTP(phone: String) {
        createNetworkRequest { sessionRepository.sendOTP(phone) }.showLoading().onSuccess {
            sendChangePasswordOtpResult.value = it
        }.execute()
    }

    val changeResult = MutableLiveData<AccessSessionResponse?>()
    fun changePassword(phone: String, code: String, password: String) {
        createNetworkRequest {
            sessionRepository.changePassword(phone, code, password)
        }.showLoading().onSuccess {
            changeResult.value = it
        }.execute()
    }

    val setPwdResult = MutableLiveData<AccessSessionResponse?>()
    fun setPassword(phone: String, password: String) {
        createNetworkRequest {
            sessionRepository.setPassword(phone, password)
        }.showLoading().onSuccess {
            submitTrackingEvent(
                TrackBean(
                    p = PageCreatePassword,
                    act = ACT_createPassword,
                    result = it.toJsonString()
                )
            )
            setPwdResult.value = it
        }.onFailed {
            submitTrackingEvent(
                TrackBean(
                    p = PageCreatePassword,
                    act = ACT_createPassword,
                    result = it.toJsonString()
                )
            )
            false
        }
    }
}
