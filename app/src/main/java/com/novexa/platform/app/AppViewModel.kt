package com.novexa.platform.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.novexa.platform.core.ui.base.BaseViewModel
import com.novexa.platform.core.common.data.ACT_UserAppUserDevice
import com.novexa.platform.core.common.data.ACT_UserAppUserDeviceHasDevice
import com.novexa.platform.core.common.data.bean.ServiceResponse
import com.novexa.platform.core.common.data.bean.Event
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.core.common.data.isPostDeviceInfo
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.core.network.NetworkCredentialStore
import com.novexa.platform.core.device.RiskSnapshotCollector
import com.novexa.platform.core.common.util.PermissionCoordinator
import com.novexa.platform.core.common.util.PermissionScenario
import com.novexa.platform.core.common.util.text.toJsonString
import kotlinx.coroutines.launch

class AppViewModel(
    private val appConfigRepository: AppConfigRepository = AppConfigRepository(),
) : BaseViewModel() {
    val errorResponse = MutableLiveData<Event<ServiceResponse<*>?>>()
    val isShowLoading = MutableLiveData<Boolean>()

    val secretResult = MutableLiveData<String?>()
    val secretRequestResult = MutableLiveData<Long>()
    private var secretRequestId = 0L

    fun getAppSecret(): Long {
        val requestId = ++secretRequestId
        createNetworkRequest { appConfigRepository.fetchAppSecret() }.onSuccess {
            if (!it?.verifySignSecret.isNullOrBlank()) {
                NetworkCredentialStore.signingSecret = it.verifySignSecret
            }
            secretResult.value = it?.verifySignSecret
            secretRequestResult.value = requestId
        }.execute()
        return requestId
    }

    fun hasDeviceInfo(pageString: String, action: (Boolean) -> Unit) {
        if (!SessionStore.isLoggedIn) return
        createNetworkRequest { appConfigRepository.hasUploadedDevice() }.onSuccess {
            submitTrackingEvent(
                TrackBean(
                    p = pageString,
                    act = ACT_UserAppUserDeviceHasDevice,
                    result = it.toJsonString()
                )
            )
            isPostDeviceInfo = it == true
            action.invoke(it == true)
        }.onFailed {
            submitTrackingEvent(
                TrackBean(
                    p = pageString,
                    act = ACT_UserAppUserDeviceHasDevice,
                    result = it.toJsonString()
                )
            )
            isPostDeviceInfo = false
            action(false)
            false
        }
    }

    private var postingDevice: Boolean = false
    fun postRiskInfo(
        pageString: String,
        action: (Boolean) -> Unit
    ) {
        if (!SessionStore.isLoggedIn || !PermissionCoordinator.hasAll(
                MainApplication.appContext,
                PermissionScenario.DEVICE_RISK,
            ) || postingDevice
        ) return
        postingDevice = true
        if (isPostDeviceInfo) {
            action(true)
            postingDevice = false
            return
        }
        viewModelScope.launch {
            val riskJson = RiskSnapshotCollector.collect()
            createNetworkRequest {
                appConfigRepository.uploadRiskInfo(riskJson)
            }.showLoading().onSuccess {
                isPostDeviceInfo = true
                submitTrackingEvent(
                    TrackBean(
                        p = pageString,
                        act = ACT_UserAppUserDevice,
                        result = it.toJsonString()
                    )
                )
                action(true)
                postingDevice = false
            }.onFailed {
                submitTrackingEvent(
                    TrackBean(
                        p = pageString,
                        act = ACT_UserAppUserDevice,
                        result = it.toJsonString()
                    )
                )
                action(false)
                postingDevice = false
                false
            }
        }

    }
}
