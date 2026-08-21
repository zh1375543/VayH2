package com.velora.portal.platform.design.base

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.velora.portal.application.MainApplication
import com.velora.portal.platform.common.data.ACT_back
import com.velora.portal.platform.common.data.ACT_copy
import com.velora.portal.platform.common.data.ACT_paste
import com.velora.portal.platform.common.data.PageAll
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.journey.access.presentation.login.AccountAccessActivity
import com.velora.portal.platform.common.util.AppStackUtil
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.application.LaunchActivity
import com.velora.portal.platform.design.dialog.createLoadingDialog
import com.velora.portal.platform.design.dialog.createVersionUpdateDialog
import com.velora.portal.platform.design.component.StyledEditTextView
import com.velora.portal.platform.common.util.platform.configureSystemBars
import com.velora.portal.platform.common.util.start
import kotlinx.coroutines.launch
import java.util.Locale

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var permissionResultCallback: ((Map<String, Boolean>) -> Unit)? = null
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val callback = permissionResultCallback
        permissionResultCallback = null
        callback?.invoke(result)
    }

    private val loadingDialog by lazy { createLoadingDialog() }

    private val updateDialog by lazy { createVersionUpdateDialog() }

    protected abstract val binding: VB
    protected open val adjustForImeInsets: Boolean = true

    /** Updates system-bar icon contrast for screens with a dark or light top background. */
    protected fun setLightSystemBarIcons(enabled: Boolean) {
        configureSystemBars(darkMode = !enabled, adjustForIme = adjustForImeInsets)
    }

    /** Launches the platform runtime-permission dialog for PermissionCoordinator. */
    fun launchRuntimePermissions(
        permissions: Array<String>,
        onResult: (Map<String, Boolean>) -> Unit,
    ) {
        check(permissionResultCallback == null) { "A permission request is already in progress" }
        permissionResultCallback = onResult
        permissionLauncher.launch(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightSystemBarIcons(enabled = false)
        AppStackUtil.addActivity(this)
        initView()
        initObserve()
        observeGlobalViewModel()
        setupPasteListener(findViewById(android.R.id.content))
        setupClipboardListener()
    }

    private fun showLoading() {
        lifecycleScope.launch {
            loadingDialog.show()
        }
    }

    private fun hideLoading() {
        lifecycleScope.launch {
            loadingDialog.dismiss()
        }
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val configuration = resources.configuration
        configuration.setLayoutDirection(configuration.locale)
        configuration.fontScale = 1f
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return resources
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        configuration.setLocale(Locale.US)
        val ctx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(Locale.US))
            newBase.createConfigurationContext(configuration)
        } else {
            val resources = newBase.resources
            val dm = resources.displayMetrics
            resources.updateConfiguration(configuration, dm)
            newBase
        }
        super.attachBaseContext(ctx)
    }

    fun logOut(isToLogin: Boolean = true) {
        lifecycleScope.launch {
            SessionStore.clear()
            if (isToLogin) {
                start<AccountAccessActivity>()
            }
        }
    }

    abstract fun initView()

    open fun initObserve() {}

    protected fun applyTopInset(target: View) {
        val startPadding = target.paddingStart
        val topPadding = target.paddingTop
        val endPadding = target.paddingEnd
        val bottomPadding = target.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPaddingRelative(
                startPadding,
                topPadding + systemBars.top,
                endPadding,
                bottomPadding
            )
            insets
        }
        ViewCompat.requestApplyInsets(target)
    }
    

    private fun observeGlobalViewModel() {
        if (this is LaunchActivity) return
        MainApplication.appViewModel.isShowLoading.observe(this) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
        MainApplication.appViewModel.errorResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let { response ->
                when (response.code) {
                    300 -> {
                        updateDialog.show()
                    }

                    401, 402 -> {
                        response.message.showToastMessage()
                        logOut(true)
                    }

                    409 -> {
                        MainApplication.appViewModel.getAppSecret()
                    }

                    else -> {
                        if (!response.message.isNullOrBlank() && !response.disabledToast)
                            response.message.showToastMessage()
                    }
                }
            }
        }
    }

    fun registerTrackedBackHandler(vm: BaseViewModel? = null, action: () -> Unit) {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vm?.submitTrackingEvent(
                    TrackBean(
                        act = ACT_back,
                        p = localClassName
                    )
                )
                action()
            }
        })
    }

    /** Lets a screen keep the IME open while a keyboard-attached action receives its click. */
    protected open fun shouldDismissKeyboardOnOutsideTouch(ev: MotionEvent): Boolean = true

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt()) &&
                    shouldDismissKeyboardOnOutsideTouch(ev)
                ) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun setupPasteListener(root: ViewGroup) {
        traverseView(root) { editText ->
            (editText as? StyledEditTextView)?.onPasteListener = { content ->
                MainApplication.appViewModel.submitTrackingEvent(
                    TrackBean(
                        p = PageAll,
                        act = ACT_paste,
                        result = System.currentTimeMillis().toString() + "|" + content
                    )
                )
            }
        }
    }

    private fun traverseView(view: View, callback: (View) -> Unit) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                traverseView(view.getChildAt(i), callback)
            }
        }
        callback(view)
    }

    private val clipboard by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }
    val listener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipData = clipboard.primaryClip
        val content = clipData?.getItemAt(0)?.text.toString()

        MainApplication.appViewModel.submitTrackingEvent(
            TrackBean(
                p = PageAll,
                act = ACT_copy,
                result = System.currentTimeMillis().toString() + "|" + content
            )
        )
    }

    private fun setupClipboardListener() {
        clipboard.addPrimaryClipChangedListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboard.removePrimaryClipChangedListener(listener)
    }
}
