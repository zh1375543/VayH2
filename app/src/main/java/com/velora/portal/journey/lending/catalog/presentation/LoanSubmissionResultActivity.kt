package com.velora.portal.journey.lending.catalog.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.velora.portal.application.MainApplication
import com.velora.portal.BuildConfig
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.PageProductDetail
import com.velora.portal.platform.common.data.APPCODE
import com.velora.portal.platform.common.data.location
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.platform.common.data.signBackHome
import com.velora.portal.databinding.ScreenLoanSubmissionResultBinding
import com.velora.portal.domain.credit.model.MemberOverviewResponse
import com.velora.portal.domain.credit.model.CatalogEntry
import com.velora.portal.application.PortalHostActivity
import com.velora.portal.journey.lending.dashboard.presentation.adapter.LoanCatalogAdapter
import com.velora.portal.journey.lending.dashboard.presentation.state.HomeProductUi
import com.velora.portal.journey.lending.catalog.presentation.adapter.ApplicationResultAdapter
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.AppStackUtil
import com.velora.portal.platform.common.util.ExternalActionLauncher
import com.velora.portal.platform.common.util.LOAN_GET_NOW_CLICK
import com.velora.portal.platform.common.util.loanevent.LoanEventRecorder
import com.velora.portal.platform.common.util.LogUtil
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.PermissionScenario
import com.velora.portal.platform.common.util.generateRequestBody
import com.velora.portal.platform.common.util.getLocalIpAddress
import com.velora.portal.platform.common.util.isPositive
import com.velora.portal.platform.common.util.text.parseJson
import com.velora.portal.platform.telemetry.device.DeviceIdentityReader
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.journey.lending.dashboard.presentation.dialog.showCreditUnderReviewDialog
import com.velora.portal.journey.lending.dashboard.presentation.dialog.showPreCreditExpiredDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import kotlinx.coroutines.launch
import kotlin.toString

class LoanSubmissionResultActivity :
    BaseActivity<ScreenLoanSubmissionResultBinding>() {

    override val binding by viewBinding(ScreenLoanSubmissionResultBinding::inflate)
    companion object {
        fun launch(
            context: Context,
            productList: ArrayList<CatalogEntry>?,
            productId: String?,
            bankId: Long?,
            signPath: String?,
            amount: String?,
            productInstallmentMap: String? = null,
            termIdMap: String? = null,
            payWay: String = "CARD",
        ) {
            context.start<LoanSubmissionResultActivity> {
                putExtra("productList", productList)
                putExtra("bankId", bankId)
                putExtra("signPath", signPath)
                putExtra("amount", amount)
                putExtra("productId", productId)
                putExtra("productInstallmentMap", productInstallmentMap)
                putExtra("termIdMap", termIdMap)
                putExtra("payWay", payWay)
            }
        }
    }

    private val vm by viewModels<ApplicationProcessViewModel>()
    private val productVm by viewModels<ProductOptionsViewModel>()

    private val productList by lazy {
        intent.getParcelableArrayListExtra<CatalogEntry>("productList")
    }
    private val termIdMap by lazy { intent.getStringExtra("termIdMap") }
    private val bankId by lazy { intent.getLongExtra("bankId", 0L) }
    private val signPath by lazy { intent.getStringExtra("signPath") }
    private val amount by lazy { intent.getStringExtra("amount") }
    private val productId by lazy { intent.getStringExtra("productId") }
    private val productInstallmentMap by lazy { intent.getStringExtra("productInstallmentMap") }
    private val payWay by lazy { intent.getStringExtra("payWay") ?: "CARD" }
    private val resultAdapter by lazy {
        ApplicationResultAdapter()
    }
    private val homeAdapter by lazy {
        LoanCatalogAdapter().apply {
            setOnChildClickListener { view, _, position ->
                if (view.id == R.id.tvApply) {
                    items.getOrNull(position)?.let { item ->
                        handleRecommendedProductClick(item)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun initView() = with(binding) {
        setStatusBarAppearance(
            statusBarColor = R.color.brand_primary,
            useDarkStatusBarIcons = false,
        )
        registerTrackedBackHandler(vm) {
            returnToDashboard()
        }
        titleBar.setNavigationAction { returnToDashboard() }
        tvWithdrawal.singleClick {
            openCombinedLoanOffer()
        }
        rvProduct.adapter = resultAdapter
        rvCashableProduct.adapter = homeAdapter
        pageContent.isVisible = false
        pageState.showLoading()
        initRisk()
        if (location.first == 0.0) {
            PermissionCoordinator.request(this@LoanSubmissionResultActivity, PermissionScenario.DEVICE_RISK) {
                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                    location = it.longitude to it.latitude
                }
            }
        }
    }

    private fun returnToDashboard() {
        AppStackUtil.finishActivity(MultiLoanOfferActivity::class.java)
        AppStackUtil.finishActivity(SignatureCaptureActivity::class.java)
        AppStackUtil.finishActivity(LoanProductDetailActivity::class.java)
        finish()
        PortalHostActivity.Companion.launch(this)
    }

    private fun handleRecommendedProductClick(item: HomeProductUi) {
        trackEvent(LOAN_GET_NOW_CLICK)
        if (!item.canApply) return

        val product = item.product
        if (product.creditStatus == 2) {

            showPreCreditExpiredDialog(product.enableLoanStr.orEmpty())
            return
        }
        if (product.creditStatus == 0) {
            showCreditUnderReviewDialog()
            return
        }

        when (product.jumpType) {
            1 -> product.downloadUrl?.let {
                ExternalActionLauncher.openBrowser(this, it)
            }
            2 -> ExternalActionLauncher.openStoreListing(this, product.downloadUrl)
            4 -> openCombinedLoanOffer()
            else -> productVm.getProductDetail(
                PageHome,
                product.productId.toString(),
                product.maxLoanAmount.toString(),
                true,
            ) {}
        }
    }

    private fun refreshRecommendedProducts() {
        binding.apply {
            cashableProductLayout.isVisible = false
            tvWithdrawal.isVisible = false
            updateResultsCardVisibility()
        }
        homeAdapter.submitItems(emptyList())
        vm.getTogetherLoan(showLoading = true) {
            collapseOfferRecommendations()
        }
    }

    private fun updateRecommendedProducts(data: MemberOverviewResponse?) {
        val products = data?.showProducts.orEmpty().onEach { product ->
            product.canApply = true
            product.isTogether = true
            if (product.currency == null) product.currency = data?.currency
            if (product.currencySymbol == null) product.currencySymbol = data?.currencySymbol
        }
        homeAdapter.submitItems(products.map { product ->
            HomeProductUi(product = product, canApply = product.canApply)
        })
        val hasCashableProducts =
            data?.canApplyAmount.isPositive() &&
                products.isNotEmpty()
        binding.apply {
            cashableProductLayout.isVisible = hasCashableProducts
            tvWithdrawal.isVisible = hasCashableProducts
            updateResultsCardVisibility()
        }
    }

    private fun collapseOfferRecommendations() = with(binding) {
        cashableProductLayout.isVisible = false
        tvWithdrawal.isVisible = false
        updateResultsCardVisibility()
    }

    private fun updateResultsCardVisibility() = with(binding) {
        resultsCard.isVisible = rvProduct.isVisible || cashableProductLayout.isVisible
    }

    private fun handleProductDetail(data: CatalogEntry?) {
        data ?: return
        finishPreviousLoanFlow()
        start<LoanProductDetailActivity> {
            putExtra("product", data)
        }
    }

    private fun openCombinedLoanOffer() {
        finishPreviousLoanFlow()
        start<MultiLoanOfferActivity>()
        finish()
    }

    private fun finishPreviousLoanFlow() {
        AppStackUtil.finishActivity(MultiLoanOfferActivity::class.java)
        AppStackUtil.finishActivity(SignatureCaptureActivity::class.java)
        AppStackUtil.finishActivity(LoanProductDetailActivity::class.java)
    }
    private fun startLoan(eventFile: File?) = with(binding) {
//        LogUtil.e("signature image provided: $signPath")
        val builder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.Companion.FORM)
        if (signPath != null) {
//                val signPic = File(cacheDir, "test.jpeg")
            val signPic = File(signPath!!)
            if (signPic.exists()) {
                val imgFileRQ = RequestBody.Companion.create("image/*".toMediaTypeOrNull(), signPic)
                val imgPart = MultipartBody.Part.Companion.createFormData("signPic", signPic.name, imgFileRQ)
                builder.addPart(imgPart)
//                    LogUtil.e("signature image provided")
            }
        }
        if (eventFile?.exists() == true) {
            val fileRQ = RequestBody.Companion.create("text/plain".toMediaTypeOrNull(), eventFile)
            val part = MultipartBody.Part.Companion.createFormData("eventFile", eventFile.name, fileRQ)
            builder.addPart(part)
        }
        val parts: List<MultipartBody.Part> = builder.build().parts

        val map = HashMap<String, String>()
        map["mobileType"] = "2"
        map["appCode"] = APPCODE
        map["version"] = BuildConfig.VERSION_NAME
        map["userId"] = SessionStore.loginInfo?.id.toString()
        map["payWay"] = payWay
        if (payWay == "CARD") {
            map["bankInfoId"] = bankId.toString()
        } else {
            map["userCashWalletId"] = bankId.toString()
        }
        map["ip"] = getLocalIpAddress() ?: ""
        map["imei"] = DeviceIdentityReader.getDeviceId()
        map["coordinate"] =
            "${location.first},${location.second}"
        map["auditKey"] = "auditKey"
        if (productList != null) {
            if (productInstallmentMap != null) {
                map["productInstallmentMap"] = productInstallmentMap!!
            }
            if (termIdMap != null) {
                map["productLoanTermIdMap"] = termIdMap!!
            }
            LogUtil.e("productLoanTermIdMap:$termIdMap")
            map["productIds"] =
                productList!!.joinToString(",") { it1 -> it1.productId.toString() }
            val mBody = map.generateRequestBody()
            vm.togetherLoan(parts, mBody)
        } else {
            LogUtil.e("termId:$termIdMap")
            if (productInstallmentMap != null) {
                try {
                    val obj = productInstallmentMap.parseJson<Map<Long?, Double?>>()
                    val planNums = obj?.values?.firstOrNull()?.toInt()
                    LogUtil.e("planNums:$planNums")
                    if (planNums != null) {
                        map["planNums"] = planNums.toString()
                    }
                } catch (e: Exception) {
                    LogUtil.e("planNumsEx:${e.message}")
                }
            }
            if (termIdMap != null) {
                try {
                    val obj = termIdMap.parseJson<Map<Long?, Double?>>()
                    val termId = obj?.values?.firstOrNull()?.toLong()
                    LogUtil.e("termId:$termId")
                    if (termId != null) {
                        map["loanTermId"] = termId.toString()
                    }
                } catch (e: Exception) {
                    LogUtil.e("termEx:${e.message}")
                }
            }
            map["productId"] = productId.toString()
            map["amount"] = amount.toString()
            val mBody = map.generateRequestBody()
            vm.loan(parts, mBody)
        }
    }

    private fun initRisk() {
        MainApplication.Companion.appViewModel.hasDeviceInfo(PageProductDetail) {
            if (it) {
                getEventFile { file ->
                    startLoan(file)
                }
                return@hasDeviceInfo
            }
            MainApplication.Companion.appViewModel.postRiskInfo(
                PageProductDetail
            ) { isSuccess ->
                if (isSuccess) {
                    getEventFile { file ->
                        startLoan(file)
                    }
                } else {
                    loanFailed()
                }
            }
        }
    }

    private fun getEventFile(action: (File?) -> Unit) {
        lifecycleScope.launch {
            action(LoanEventRecorder.prepareUploadFile())
        }
    }

    private fun loanSuccess() {
        binding.apply {
            pageContent.isVisible = true
            pageState.hide()
            successLayout.isVisible = true
            failLayout.isVisible = false
            tvLoanResultTip.isVisible = false
            ivSuccess.isVisible = true
            ivFail.isVisible = false
            updateResultsCardVisibility()
        }
        signBackHome = false
        refreshRecommendedProducts()
    }

    private fun loanFailed() {
        binding.apply {
            pageContent.isVisible = true
            pageState.hide()
            successLayout.isVisible = false
            failLayout.isVisible = true
            tvLoanResultTip.isVisible = true
            ivSuccess.isVisible = false
            ivFail.isVisible = true
            updateResultsCardVisibility()
        }
        signBackHome = false
        refreshRecommendedProducts()
    }

    override fun initObserve() {
        super.initObserve()
        vm.loanResult.observe(this@LoanSubmissionResultActivity) {
            binding.rvProduct.isVisible = false
            loanSuccess()
        }
        vm.loanFailResult.observe(this@LoanSubmissionResultActivity) {
            loanFailed()
        }
        vm.togetherLoanResult.observe(this@LoanSubmissionResultActivity) {
            resultAdapter.submitItems(it?.onEach { it1 ->
                it1.currency = productList?.get(0)?.currency
                it1.currencySymbol = productList?.get(0)?.currencySymbol
            })
            binding.rvProduct.isVisible = !it.isNullOrEmpty()
            loanSuccess()
        }
        vm.togetherInfo.observe(this@LoanSubmissionResultActivity) {
            updateRecommendedProducts(it)
        }
        productVm.detailResult.observe(this@LoanSubmissionResultActivity) {
            handleProductDetail(it)
        }
    }
}
