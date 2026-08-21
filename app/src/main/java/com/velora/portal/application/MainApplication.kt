package com.velora.portal.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.initialize
import com.google.firebase.messaging.FirebaseMessaging
import com.liveness.dflivenesslibrary.DFProductResult
import com.liveness.dflivenesslibrary.DFTransferResultInterface
import com.velora.portal.BuildConfig
import com.velora.portal.platform.common.data.ACT_EnterApp
import com.velora.portal.platform.common.data.ACT_EnterBackground
import com.velora.portal.platform.common.data.PageEnter
import com.velora.portal.platform.common.data.PageExit
import com.velora.portal.platform.common.data.AF_DEV_KEY
import com.velora.portal.platform.common.data.afSource
import com.velora.portal.platform.common.data.appFlyer
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.common.data.firebaseId
import com.velora.portal.platform.common.data.firebaseToken
import com.velora.portal.platform.common.data.gaId
import com.velora.portal.platform.common.data.refer
import com.velora.portal.platform.network.NetworkCredentialStore
import com.velora.portal.platform.common.util.LogUtil
import com.velora.portal.platform.common.util.platform.AdvertisingIdProvider
import com.velora.portal.platform.common.util.platform.AppFontInstaller
import com.velora.portal.platform.common.util.text.toJsonString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.iterator
import kotlin.properties.Delegates

class MainApplication : Application(), DFTransferResultInterface {

    companion object {
        var appContext: Context by Delegates.notNull()
        lateinit var instance: MainApplication
        lateinit var appViewModel: AppViewModel
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        instance = this
        AppFontInstaller.install(this, "fonts/medium.otf", "fonts/bold.otf")
        startAppsFlyer()
        bootstrapFirebase()
        setupAppViewModel()
        fetchAdId()
        registerAppLifecycle()
        setupAppCheck()
    }

    private fun setupAppCheck() {
        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        FirebaseAppCheck.getInstance()
            .getAppCheckToken(false)
            .addOnSuccessListener { result ->
                val token = result.token
                Log.e("APPCheck", token)
                NetworkCredentialStore.appCheckToken = token
            }
            .addOnFailureListener { e ->
                Log.e("APPCheckFailed", e.message.toString())
            }

        fetchFcmToken()
        fetchFirebaseAnalyticsId()
    }

    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    LogUtil.d("Fetch FCM token failed:" + task.exception?.message)
                    return@addOnCompleteListener
                }

                firebaseToken = task.result
            }
    }

    private fun fetchFirebaseAnalyticsId() {
//        FirebaseInstallations.getInstance().id
        FirebaseAnalytics.getInstance(this).appInstanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseClientId = task.result
                    LogUtil.e("Firebase Installation ID: $firebaseClientId")
                    firebaseId = firebaseClientId
                } else {
                    LogUtil.e("Get Firebase ID failed:" + task.exception?.message)
                }
            }
    }

    fun startAppsFlyer() {
        val listener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(map: MutableMap<String, Any>?) {
                LogUtil.e("AppFlyerSuccess")
                if (map == null) return
                for ((key, value) in map) {
                    if (key == "media_source") {
                        afSource = value.toString()
                    }
                }
                appFlyer = map.toJsonString()
            }

            override fun onConversionDataFail(p0: String?) {
                LogUtil.e("AppFlyerFailed:$p0")
            }

            override fun onAppOpenAttribution(p0: Map<String?, String?>?) {

            }

            override fun onAttributionFailure(p0: String?) {
            }
        }
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG)
        AppsFlyerLib.getInstance().init(AF_DEV_KEY, listener, this)
        AppsFlyerLib.getInstance().setCollectAndroidID(false)
        AppsFlyerLib.getInstance().setCollectIMEI(false)
        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance().registerConversionListener(this, listener)
        // google refer
        InstallReferrerClient.newBuilder(this).build().let { referrerClient ->
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    if (responseCode ==
                        InstallReferrerClient.InstallReferrerResponse.OK
                    ) {
                        val ref =
                            referrerClient.installReferrer?.installReferrer
                        LogUtil.e("ref:$ref")
                        refer = ref ?: ""
                    }
                    referrerClient.endConnection()
                }

                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }
    }

    private fun setupAppViewModel() {
        appViewModel = ViewModelProvider(
            ViewModelStore(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        )[AppViewModel::class.java]
    }

    private fun fetchAdId() {
        CoroutineScope(Dispatchers.IO).launch {
            gaId = AdvertisingIdProvider.get(this@MainApplication)
        }
    }

    private var result: DFProductResult? = null
    override fun setResult(p0: DFProductResult?) {
        result = p0
    }

    override fun getResult(): DFProductResult? {
        return result
    }

    private fun bootstrapFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerAppLifecycle() {
        val callback = AppLifecycleTracker()
        registerActivityLifecycleCallbacks(callback)
        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(callback)
    }

    private class AppLifecycleTracker : DefaultLifecycleObserver, ActivityLifecycleCallbacks {
        private var startedActivityCount = 0
        private var lastStopWasConfigChange = false

        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        }

        override fun onActivityDestroyed(p0: Activity) {
        }

        override fun onActivityPaused(p0: Activity) {
        }

        override fun onActivityResumed(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityStarted(p0: Activity) {
            if (++startedActivityCount == 1 && !lastStopWasConfigChange) {
                // MainApplication enters foreground from background
//                MyLogUtil.d("MainApplication entered foreground")
                appViewModel.submitTrackingEvent(
                    TrackBean(
                        act = ACT_EnterApp,
                        result = p0.localClassName,
                        p = PageEnter
                    )
                )
            }
        }

        override fun onActivityStopped(p0: Activity) {
            lastStopWasConfigChange = p0.isChangingConfigurations
            if (--startedActivityCount == 0 && !lastStopWasConfigChange) {
                // MainApplication enters background from foreground
//                MyLogUtil.d("MainApplication entered background")
                appViewModel.submitTrackingEvent(
                    TrackBean(
                        act = ACT_EnterBackground,
                        result = p0.localClassName,
                        p = PageExit
                    )
                )
            }
        }
    }
}
