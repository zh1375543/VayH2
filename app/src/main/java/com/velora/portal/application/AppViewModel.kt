package com.velora.portal.application

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.velora.portal.platform.design.base.BaseViewModel
import com.velora.portal.platform.common.data.ACT_UserAppUserDevice
import com.velora.portal.platform.common.data.ACT_UserAppUserDeviceHasDevice
import com.velora.portal.platform.common.data.bean.ServiceResponse
import com.velora.portal.platform.common.data.bean.Event
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.common.data.isPostDeviceInfo
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.platform.network.NetworkCredentialStore
import com.velora.portal.platform.telemetry.device.RiskSnapshotCollector
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.PermissionScenario
import com.velora.portal.platform.common.util.text.toJsonString
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
