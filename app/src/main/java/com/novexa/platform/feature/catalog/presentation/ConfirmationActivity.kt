package com.novexa.platform.feature.catalog.presentation

import android.content.Context
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.novexa.platform.app.MainApplication
import com.novexa.platform.R
import com.novexa.platform.feature.onboarding.presentation.login.AccessSessionViewModel
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.core.common.data.ACT_clickSubmit
import com.novexa.platform.core.common.data.ACT_in
import com.novexa.platform.core.common.data.PageSign
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.core.common.data.signBackHome
import com.novexa.platform.databinding.ActivityAgreementSignatureBinding
import com.novexa.platform.feature.catalog.model.CatalogItemBean
import com.novexa.platform.app.MainActivity
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.ui.component.SignatureView
import com.novexa.platform.core.common.util.loanevent.LoanEvent
import com.novexa.platform.core.common.util.loanevent.LoanEventRecorder
import com.novexa.platform.core.common.util.PermissionCoordinator
import com.novexa.platform.core.common.util.PermissionScenario
import com.novexa.platform.core.common.util.platform.configureSystemBars
import com.novexa.platform.core.common.util.showToastMessage
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ConfirmationActivity : BaseActivity<ActivityAgreementSignatureBinding>() {

    override val binding by viewBinding(ActivityAgreementSignatureBinding::inflate)
    companion object {

        fun launch(
            context: Context,
            cardId: Long?,
            productList: ArrayList<CatalogItemBean>?,
            productId: String?,
            bankId: Long?,
            amount: String?,
            productInstallmentMap: String?,
            termIdMap: String?,
            isBackHome: Boolean = false,
            payWay: String = "CARD",
        ) {
            context.start<ConfirmationActivity> {
                putExtra("isBackHome", isBackHome)
                putExtra("productList", productList)
                putExtra("bankId", bankId)
                putExtra("amount", amount)
                putExtra("productId", productId)
                putExtra("bankId", cardId)
                putExtra("productInstallmentMap", productInstallmentMap)
                putExtra("termIdMap", termIdMap)
                putExtra("payWay", payWay)
            }
        }
    }

    private val vm by viewModels<AccessSessionViewModel>()

    private val isShowBackHome by lazy {
        intent.getBooleanExtra("isBackHome", false)
    }
    private val productList by lazy {
        intent.getParcelableArrayListExtra<CatalogItemBean>("productList")
    }
    private val bankId by lazy { intent.getLongExtra("bankId", 0L) }
    private val amount by lazy { intent.getStringExtra("amount") }
    private val productId by lazy { intent.getStringExtra("productId") }
    private val productInstallmentMap by lazy { intent.getStringExtra("productInstallmentMap") }
    private val termIdMap by lazy { intent.getStringExtra("termIdMap") }
    private val payWay by lazy { intent.getStringExtra("payWay") ?: "CARD" }

    private var isSign = false

    override fun initView() {
        renderSignatureWorkspace()
        connectSigningCommands()
    }

    private fun renderSignatureWorkspace() = with(binding) {
        configureSystemBars(darkMode = true)
        vm.submitTrackingEvent(TrackBean(p = PageSign, act = ACT_in))
        if (isShowBackHome) {
            LoanEventRecorder.setEventFileSuffix((SessionStore.loginInfo?.id ?: 111).toString())
        }
        tvBack.visibility = if (isShowBackHome) View.VISIBLE else View.INVISIBLE
        tvSign.visibility = tvBack.visibility
        tvSign2.visibility = if (!isShowBackHome) View.VISIBLE else View.GONE
        signView.setOnSignatureListener(object : SignatureView.OnSignatureListener {
            override fun onStartSigning() {
                tvHint.isVisible = false
                isSign = true
            }

            override fun onCleared() {
                tvHint.isVisible = true
                isSign = false
            }

        })
    }

    private fun connectSigningCommands() = with(binding) {
        titleBar.setNavigationAction { exitSignatureFlow() }
        tvBack.singleClick {
            MainActivity.Companion.launch(this@ConfirmationActivity)
            exitSignatureFlow()
        }
        registerTrackedBackHandler(vm) {
            exitSignatureFlow()
        }
//            if (CacheManager.signFile.exists() && CacheManager.signFile.length() > 0) {
//                setResult(
//                    RESULT_OK, Intent()
//                        .putExtra("filePath", CacheManager.signFile.absolutePath)
//                )
//                finish()
//            }
        tvSign2.singleClick {
            tvSign.performClick()
        }
        tvSign.singleClick {
            if (!isSign) {
                getString(R.string.please_sign).showToastMessage()
                return@singleClick
            }
            vm.submitTrackingEvent(TrackBean(p = PageSign, act = ACT_clickSubmit))
            if (isShowBackHome) {
                LoanEventRecorder.record(LoanEvent.CLICK_APPLY_LOAN)
                PermissionCoordinator.request(this@ConfirmationActivity, PermissionScenario.DEVICE_RISK) {
                    MainApplication.Companion.appViewModel.postRiskInfo(PageSign) { isSuccess ->
                        if (isSuccess) {
                            LoanEventRecorder.record(LoanEvent.CLICK_SUBMIT_LOAN)
                            submitSignedAgreement()
                        }
                    }
                }
            } else {
                submitSignedAgreement()
            }
        }
    }

    private fun exitSignatureFlow() {
        if (isShowBackHome) {
            signBackHome = true
            MainActivity.Companion.launch(this)
        }
        finish()
    }

    private fun submitSignedAgreement() {
        lifecycleScope.launch {
            val file =
                File(MainApplication.Companion.appContext.cacheDir, "sign_${System.currentTimeMillis()}.png")
            if (withContext(Dispatchers.IO) {
                    binding.signView.saveToFile(file)
                }) {
                finish()
                RequestStatusActivity.Companion.launch(
                    this@ConfirmationActivity,
                    productList,
                    productId,
                    bankId,
                    file.absolutePath,
                    amount,
                    productInstallmentMap,
                    termIdMap,
                    payWay
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isShowBackHome) {
            LoanEventRecorder.initializeBaseServerTime(System.currentTimeMillis())
            LoanEventRecorder.record(LoanEvent.VIEW_ENTER_LOAN)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isShowBackHome) {
            LoanEventRecorder.record(LoanEvent.VIEW_QUIT_LOAN)
            LoanEventRecorder.flush()
        }
    }
}
