package com.novexa.platform.feature.onboarding.data

import android.os.Build
import com.appsflyer.AppsFlyerLib
import com.novexa.platform.BuildConfig
import com.novexa.platform.app.MainApplication
import com.novexa.platform.core.common.data.appFlyer
import com.novexa.platform.core.common.data.bean.ApiRequest
import com.novexa.platform.core.common.data.firebaseId
import com.novexa.platform.core.common.data.firebaseToken
import com.novexa.platform.core.common.data.location
import com.novexa.platform.core.network.Api
import com.novexa.platform.core.network.NetworkProvider
import com.novexa.platform.core.common.data.repository.dataOrThrow
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.feature.onboarding.model.AccessSessionResponse
import com.novexa.platform.core.device.DeviceIdentityReader
import com.novexa.platform.core.common.util.toMd5

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

        return api.sendSMS(param).dataOrThrow()
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
        return api.login(param).dataOrThrow()
    }

    suspend fun logout(): Any? {
        return api.logout(ApiRequest(rid = SessionStore.loginInfo?.id)).dataOrThrow()
    }

    suspend fun setPassword(phone: String, password: String): AccessSessionResponse? {
        return api.fetchPassword(
            ApiRequest(phone = phone, newPasswd = password.toMd5())
        ).dataOrThrow()
    }

    suspend fun changePassword(phone: String, code: String, password: String): AccessSessionResponse? {
        return api.updatePassword(
            ApiRequest(phone = phone, smsCode = code, newPasswd = password.toMd5())
        ).dataOrThrow()
    }

    suspend fun postDeviceInfo(): Any? {
        if (!SessionStore.isLoggedIn) return null
        return api.postDeviceInfo(
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
