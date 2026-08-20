package com.novexa.platform.core.common.data.bean

import com.novexa.platform.BuildConfig
import com.novexa.platform.R
import com.novexa.platform.app.MainApplication
import com.novexa.platform.core.common.data.afSource
import com.novexa.platform.core.common.data.gaId
import com.novexa.platform.core.common.data.refer
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.core.device.DeviceIdentityReader
import com.novexa.platform.core.common.util.removeWhitespace

data class TrackParamBean(
    val __logs__: List<SurveyBean>,
    val __topic__: String = "survey",
)

data class SurveyBean(
    val survey: String,
)

data class TrackBean(
    val env: String = if (BuildConfig.HTTP_HOST.contains("ph-cash-api")) "dev" else "prod",
    val v: Int = 1,
    val t: String = System.currentTimeMillis().toString(),
    val m: String? = SessionStore.loginInfo?.phone,
    val p: String? = null,
    var pp: String? = null,
    var prevAct: String? = null,
    var prevP: String? = null,
    var lastAct: String? = null,
    var lastP: String? = null,
    val deviceId: String? = gaId.ifBlank { DeviceIdentityReader.getDeviceId() },
    val source: String? = afSource,
    val referer: String? = refer,
    val vestName: String? = MainApplication.appContext.resources.getString(R.string.app_name).removeWhitespace(),
    val type: String = "app",
    val act: String? = null,
    val code: String = "discarded",
    val result: String? = null,
)
