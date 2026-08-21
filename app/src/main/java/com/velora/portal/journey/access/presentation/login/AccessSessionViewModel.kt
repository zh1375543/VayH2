package com.velora.portal.journey.access.presentation.authenticate

import androidx.lifecycle.MutableLiveData
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_OTPFail
import com.velora.portal.platform.common.data.ACT_createPassword
import com.velora.portal.platform.common.data.ACT_getVerifyCode
import com.velora.portal.platform.common.data.ACT_loginOTP
import com.velora.portal.platform.common.data.ACT_loginPassword
import com.velora.portal.platform.common.data.PageCreatePassword
import com.velora.portal.platform.common.data.PageLogin
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.journey.access.data.SessionRepository
import com.velora.portal.domain.customer.model.AccessSessionResponse
import com.velora.portal.platform.common.util.text.toJsonString

class AccessSessionViewModel(
    private val sessionRepository: SessionRepository = SessionRepository(),
) : BaseViewModel() {

    val otpResult = MutableLiveData<Any?>()
    fun requestOtpCode(phone: String) {
        createNetworkRequest { sessionRepository.requestOtpCode(phone) }
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
    fun authenticate(
        phone: String,
        code: String?,
        password: String?,
    ) {
        createNetworkRequest {
            sessionRepository.authenticate(phone, code, password)
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

    fun recordDeviceSnapshot() {
        createNetworkRequest {
            sessionRepository.recordDeviceSnapshot()
        }.onSuccess { }.execute()
    }

    val logoutResult = MutableLiveData<Any?>()
    fun signOut() {
        createNetworkRequest {
            sessionRepository.signOut()
        }.showLoading().onSuccess {
            logoutResult.value = it
        }.execute()
    }

    val sendChangePasswordOtpResult = MutableLiveData<Any?>()
    fun sendChangePasswordOTP(phone: String) {
        createNetworkRequest { sessionRepository.requestOtpCode(phone) }.showLoading().onSuccess {
            sendChangePasswordOtpResult.value = it
        }.execute()
    }

    val changeResult = MutableLiveData<AccessSessionResponse?>()
    fun refreshPassword(phone: String, code: String, password: String) {
        createNetworkRequest {
            sessionRepository.refreshPassword(phone, code, password)
        }.showLoading().onSuccess {
            changeResult.value = it
        }.execute()
    }

    val setPwdResult = MutableLiveData<AccessSessionResponse?>()
    fun establishPin(phone: String, password: String) {
        createNetworkRequest {
            sessionRepository.establishPin(phone, password)
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
