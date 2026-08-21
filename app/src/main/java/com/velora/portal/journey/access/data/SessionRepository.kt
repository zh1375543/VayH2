package com.velora.portal.journey.access.data

import android.os.Build
import com.appsflyer.AppsFlyerLib
import com.velora.portal.BuildConfig
import com.velora.portal.application.MainApplication
import com.velora.portal.platform.common.data.appFlyer
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.firebaseId
import com.velora.portal.platform.common.data.firebaseToken
import com.velora.portal.platform.common.data.location
import com.velora.portal.platform.network.Api
import com.velora.portal.platform.network.NetworkProvider
import com.velora.portal.platform.common.data.repository.dataOrThrow
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.domain.customer.model.AccessSessionResponse
import com.velora.portal.platform.telemetry.device.DeviceIdentityReader
import com.velora.portal.platform.common.util.toMd5

class SessionRepository(
    private val api: Api = NetworkProvider.api,
) {

    suspend fun sendOTP(phone: String,coordinate: Pair<Double, Double> = location,): Any? {
        val param = ApiRequest(
            phone = phone,
            coordinate = "${coordinate.first},${coordinate.second}",
            regClient = "Android",
            appsflyerId = AppsFlyerLib.getInstance()
                .getAppsFlyerUID(MainApplication.Companion.appContext)
                ?: "",
            content = appFlyer,
            phoneMark = DeviceIdentityReader.getDeviceId(),
            firebaseClientId = firebaseId,
            firebaseToken = firebaseToken,
        )

        return api.requestOtpCode(param).dataOrThrow()
    }

    suspend fun login(
        phone: String,
        code: String?,
        password: String?,
        coordinate: Pair<Double, Double> = location,
    ): AccessSessionResponse? {
        val param = ApiRequest(
            phone = phone,
            coordinate = "${coordinate.first},${coordinate.second}",
            regClient = "Android",
            smsCode = code,
            appsflyerId = AppsFlyerLib.getInstance()
                .getAppsFlyerUID(MainApplication.Companion.appContext)
                ?: "",
            content = appFlyer,
            phoneMark = DeviceIdentityReader.getDeviceId(),
            passwd = password?.toMd5(),
            loginType = if (code != null) 1 else 2,
            firebaseClientId = firebaseId,
            firebaseToken = firebaseToken,
        )
        return api.authenticate(param).dataOrThrow()
    }

    suspend fun logout(): Any? {
        return api.signOut(ApiRequest(rid = SessionStore.loginInfo?.id)).dataOrThrow()
    }

    suspend fun setPassword(phone: String, password: String): AccessSessionResponse? {
        return api.establishPin(
            ApiRequest(phone = phone, newPasswd = password.toMd5())
        ).dataOrThrow()
    }

    suspend fun changePassword(phone: String, code: String, password: String): AccessSessionResponse? {
        return api.refreshPassword(
            ApiRequest(phone = phone, smsCode = code, newPasswd = password.toMd5())
        ).dataOrThrow()
    }

    suspend fun postDeviceInfo(): Any? {
        if (!SessionStore.isLoggedIn) return null
        return api.persistDeviceSnapshot(
            ApiRequest(
                phoneModel = Build.MODEL,
                phoneBrand = Build.BRAND,
                phoneMark = DeviceIdentityReader.getDeviceId(),
                appVersion = BuildConfig.VERSION_NAME,
                regClient = "Android"
            )
        ).dataOrThrow()
    }

}
