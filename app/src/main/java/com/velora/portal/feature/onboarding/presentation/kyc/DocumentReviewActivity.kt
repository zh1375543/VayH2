package com.velora.portal.feature.onboarding.presentation.kyc

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.liveness.dflivenesslibrary.liveness.DFSilentLivenessActivity
import com.velora.portal.R
import com.velora.portal.application.MainApplication
import com.velora.portal.core.ui.base.BaseActivity
import com.velora.portal.databinding.ActivityDocumentReviewBinding
import com.velora.portal.core.common.data.ACT_clickBack
import com.velora.portal.core.common.data.ACT_clickContinue
import com.velora.portal.core.common.data.ACT_clickNext
import com.velora.portal.core.common.data.ACT_in
import com.velora.portal.core.common.data.ACT_uploadBackEnd
import com.velora.portal.core.common.data.ACT_uploadBackStart
import com.velora.portal.core.common.data.ACT_uploadFaceEnd
import com.velora.portal.core.common.data.ACT_uploadFaceStart
import com.velora.portal.core.common.data.ACT_uploadFrontEnd
import com.velora.portal.core.common.data.ACT_uploadFrontStart
import com.velora.portal.core.common.data.PageInfoKyc
import com.velora.portal.core.common.data.authConfigList
import com.velora.portal.core.common.data.bean.TrackBean
import com.velora.portal.feature.onboarding.presentation.AuthStatusViewModel
import com.velora.portal.feature.onboarding.presentation.routeToNextAuthStep
import com.velora.portal.feature.onboarding.presentation.profile.ApplicantDetailsViewModel
import com.velora.portal.feature.content.presentation.ContentBrowserActivity
import com.velora.portal.core.ui.binding.bindImageUrl
import com.velora.portal.core.common.util.KYC_AADHAAR_BACK_CLICK
import com.velora.portal.core.common.util.KYC_AADHAAR_FRONT_CLICK
import com.velora.portal.core.common.util.KYC_INFO_COMMIT
import com.velora.portal.core.common.util.KYC_INFO_PAGE
import com.velora.portal.core.common.util.image.ImageProcessor
import com.velora.portal.core.ui.extension.resetScale
import com.velora.portal.core.common.util.PermissionCoordinator
import com.velora.portal.core.common.util.showToastMessage
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.util.trackEvent
import com.velora.portal.core.ui.dialog.showConfirmDialog
import com.velora.portal.feature.onboarding.presentation.dialog.showKycCardExampleDialog
import com.velora.portal.feature.onboarding.presentation.dialog.showKycSelfieExampleDialog
import com.velora.portal.core.ui.dialog.showOptionPickerDialog
import com.velora.portal.core.common.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

class DocumentReviewActivity : BaseActivity<ActivityDocumentReviewBinding>() {

    override val binding by viewBinding(ActivityDocumentReviewBinding::inflate)
    private val vm by viewModels<KycUploadViewModel>()
    private val homeVm by viewModels<AuthStatusViewModel>()
    private val personalVm by viewModels<ApplicantDetailsViewModel>()

    private val isCert by lazy { intent.getBooleanExtra("isCert", false) }
    private val frontLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            lifecycleScope.launch {
                frontUri = withContext(Dispatchers.IO) {
                    ImageProcessor.compressToCache(this@DocumentReviewActivity, photoUri)
                }
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadFrontEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
                frontUri?.let { uri -> idType?.let { vm.submitKycFront(uri, it) } }
            }
        }
    }

    private val backLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            lifecycleScope.launch {
                backUri = withContext(Dispatchers.IO) {
                    ImageProcessor.compressToCache(this@DocumentReviewActivity, photoUri)
                }
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadBackEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
                backUri?.let { uri -> idType?.let { vm.submitKycBack(uri, it) } }
            }
        }
    }
    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            lifecycleScope.launch {
                selfUri = withContext(Dispatchers.IO) {
                    ImageProcessor.compressToCache(this@DocumentReviewActivity, photoUri)
                }
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_uploadFaceEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
                selfUri?.let { vm.submitKycSelf(it, null) }
            }
        }
    }

    private val selfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            MainApplication.instance.result?.let {
                if (it.livenessImageResults.isNullOrEmpty()) return@let
                lifecycleScope.launch {
                    val (compressedUri, encryptedFile) = withContext(Dispatchers.IO) {
                        val imageFile = ImageProcessor.saveJpegToCache(
                            this@DocumentReviewActivity,
                            it.livenessImageResults[0].detectImage,
                        )
                        val imageUri = ImageProcessor.compressToCache(
                            this@DocumentReviewActivity,
                            getUri(imageFile),
                        )
                        val liveFile = ImageProcessor.saveJpegToCache(
                            this@DocumentReviewActivity,
                            it.livenessEncryptResult,
                        )
                        imageUri to liveFile
                    }
                    selfUri = compressedUri
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoKyc,
                            act = ACT_uploadFaceEnd,
                            result = System.currentTimeMillis().toString()
                        )
                    )
                    selfUri?.let { it1 ->
                        vm.submitKycSelf(
                            it1,
                            encryptedFile
                        )
                    }
                }
            }
        }
    }

    private val h5Launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            vm.getH5LiveResult()
        }
    }

    private var photoUri: Uri? = null

    private var selfUri: Uri? = null
    private var frontUri: Uri? = null
    private var backUri: Uri? = null
    private var idType: String? = null
    private var hasUploadedCardImages = false

    override fun initView() {
        prepareDocumentReviewScreen()
        bindDocumentTypeSelection()
        configureCaptureActions()
        configureKycSubmissionAndLoading()
    }

    private fun prepareDocumentReviewScreen() = with(binding) {
        configureKycMode(isEditable = !isCert)
        trackEvent(KYC_INFO_PAGE)
        vm.submitTrackingEvent(
            TrackBean(
                p = PageInfoKyc,
                act = ACT_in
            )
        )
        documentReviewHeader.setNavigationAction { handleBackPressed() }
        registerTrackedBackHandler(vm) {
            handleBackPressed()
        }
        documentReviewHeader.setAction(
            "${authConfigList.indexOf("KYC") + 1}/${authConfigList.size}"
        )
        documentReviewHeader.showAction(!isCert)
    }

    private fun bindDocumentTypeSelection() = with(binding) {
        tvDocumentExampleAction.singleClick {
            showKycCardExampleDialog()
        }
        tvSelfieExampleAction.singleClick {
            showKycSelfieExampleDialog()
        }
        documentTypeField.setOnClick {
            personalVm.getEnums { options ->
                val cardTypes = options.idCardTypeV2.orEmpty()
                if (cardTypes.isEmpty()) return@getEnums
                showOptionPickerDialog(
                    cardTypes.indexOfFirst { it.info == documentTypeField.getText() },
                    cardTypes.map { com.velora.portal.core.common.data.bean.SelectionOption(it.info.orEmpty()) },
                ) { index ->
                    documentTypeField.setText(cardTypes[index].info)
                    documentTypeField.hideError()
                    idType = cardTypes[index].state
                    updateIdImageSections()
                }
            }
        }
    }

    private fun configureKycMode(isEditable: Boolean) = with(binding) {
        documentTypeField.isVisible = isEditable
        documentTypeField.setEnableEdit(isEditable)
        ivDocumentFront.isEnabled = isEditable
        ivDocumentBack.isEnabled = isEditable
        ivSelfiePreview.isEnabled = isEditable
        tvDocumentExampleAction.isVisible = isEditable
        tvSelfieExampleAction.isVisible = isEditable
        tvSelfieInstruction.isVisible = isEditable
        submissionActionContainer.isVisible = isEditable
    }

    private fun configureCaptureActions() = with(binding) {
        ivDocumentFront.singleClick {
            if (documentTypeField.getText().isBlank()) {
                documentTypeField.showError()
                return@singleClick
            }
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoKyc,
                    act = ACT_uploadFrontStart,
                    result = System.currentTimeMillis().toString()
                )
            )
            PermissionCoordinator.request(
                this@DocumentReviewActivity,
                arrayOf(Manifest.permission.CAMERA),
            ) {
                trackEvent(KYC_AADHAAR_FRONT_CLICK)
                val outputFile = File(cacheDir, "camera_temp_${System.currentTimeMillis()}.jpg")
                photoUri = getUri(outputFile)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                setIntents(intent, false)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                frontLauncher.launch(intent)
            }
        }
        ivSelfiePreview.singleClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoKyc,
                    act = ACT_uploadFaceStart,
                    result = System.currentTimeMillis().toString()
                )
            )
            PermissionCoordinator.request(
                this@DocumentReviewActivity,
                arrayOf(Manifest.permission.CAMERA),
            ) {
                when (kycType) {
                    1 -> {
                        val outputFile =
                            File(cacheDir, "camera_temp_${System.currentTimeMillis()}.jpg")
                        photoUri = getUri(outputFile)
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        setIntents(intent, true)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        photoLauncher.launch(intent)
                    }

                    2 -> {
                        selfLauncher.launch(
                            Intent(this@DocumentReviewActivity, DFSilentLivenessActivity::class.java)
                                .putExtra(
                                    DFSilentLivenessActivity.KEY_DETECT_IMAGE_RESULT,
                                    true
                                )
                        )
                    }

                    3 -> {
                        vm.fetchH5Live {
                            selfLauncher.launch(
                                Intent(
                                    this@DocumentReviewActivity,
                                    DFSilentLivenessActivity::class.java
                                )
                                    .putExtra(
                                        DFSilentLivenessActivity.KEY_DETECT_IMAGE_RESULT,
                                        true
                                    )
                            )
                        }
                    }
                }
            }
        }
        ivDocumentBack.singleClick {
            if (documentTypeField.getText().isBlank()) {
                documentTypeField.showError()
                return@singleClick
            }
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoKyc,
                    act = ACT_uploadBackStart,
                    result = System.currentTimeMillis().toString()
                )
            )
            PermissionCoordinator.request(
                this@DocumentReviewActivity,
                arrayOf(Manifest.permission.CAMERA),
            ) {
                trackEvent(KYC_AADHAAR_BACK_CLICK)
                val outputFile = File(cacheDir, "camera_temp_${System.currentTimeMillis()}.jpg")
                photoUri = getUri(outputFile)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                setIntents(intent, false)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                backLauncher.launch(intent)
            }
        }
    }

    private fun configureKycSubmissionAndLoading() = with(binding) {
        btnSubmitReview.singleClick {
            if (documentIdentitySection.isVisible && documentTypeField.getText().isBlank()) {
                documentTypeField.showError()
                return@singleClick
            }
            if (documentFrontSection.isVisible) {
                if (frontUri == null && vm.frontImageSource.value == null) {
                    getString(R.string.please_upload_nic_card_front).showToastMessage()
                    return@singleClick
                }
            }
            if (documentBackSection.isVisible) {
                if (backUri == null && vm.backImageSource.value == null) {
                    getString(R.string.please_upload_nic_card_back).showToastMessage()
                    return@singleClick
                }
            }
            if (selfieCaptureSection.isVisible) {
                if (selfUri == null && vm.selfImageSource.value == null) {
                    getString(R.string.please_upload_self_photo).showToastMessage()
                    return@singleClick
                }
            }
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoKyc,
                    act = ACT_clickNext,
                )
            )
            trackEvent(KYC_INFO_COMMIT)
            vm.compareFace()
        }
        if (!isCert) {
            btnSubmitReview.resetScale()
        }
        pageContent.isVisible = false
        pageState.showLoading()
        pageState.setOnRetryClickListener {
            pageContent.isVisible = false
            pageState.showLoading()
            vm.getKycConfig()
        }
        vm.getKycConfig()
    }

    private var isCompare = false
    private var kycType: Int = 1
    override fun initObserve() =with(vm){
        super.initObserve()
        kycResult.observe(this@DocumentReviewActivity) {
            it?.let {
                binding.pageContent.isVisible = true
                binding.pageState.hide()
                binding.documentTypeField.setText(it.idCardType)
                hasUploadedCardImages =
                    !it.frontImageUrl.isNullOrBlank() || !it.backImageUrl.isNullOrBlank()
                resolveIdType(it.idCardType)
                updateIdImageSections()
            }
        }
        compareResult.observe(this@DocumentReviewActivity) {
            homeVm.getUserAuthStatus()
        }
        homeVm.userAuthStatusResult.observe(this@DocumentReviewActivity) {
            it?.routeToNextAuthStep(this@DocumentReviewActivity)
            finish()
        }
        configResult.observe(this@DocumentReviewActivity) {
            binding.documentIdentitySection.isVisible =
                it != null && (it.KYC_FRONT != 0 || it.KYC_BACK != 0)
            binding.selfieCaptureSection.isVisible = it != null && it.FACE != 0
            updateIdImageSections()
            isCompare = it?.FACE_COMPARE == 1
            kycType = it?.FACE ?: 1
            vm.getKycInfo {
                binding.pageContent.isVisible = false
                binding.pageState.showError()
            }
        }
        frontImageSource.observe(this@DocumentReviewActivity) {
            binding.ivDocumentFront.bindImageUrl(it)
        }
        backImageSource.observe(this@DocumentReviewActivity) {
            binding.ivDocumentBack.bindImageUrl(it)
        }
        selfImageSource.observe(this@DocumentReviewActivity) {
            binding.ivSelfiePreview.bindImageUrl(it)
        }
        frontUploadSuccess.observe(this@DocumentReviewActivity) {
            binding.frontUploadState.isVisible = it == true
            if (it == true) {
                binding.ivFrontVerifyStatus.setImageResource(R.mipmap.ic_verify_ok)
                binding.tvFrontVerifyAction.setText(R.string.re_up)
            }
        }
        backUploadSuccess.observe(this@DocumentReviewActivity) {
            binding.backUploadState.isVisible = it == true
            if (it == true) {
                binding.ivBackVerifyStatus.setImageResource(R.mipmap.ic_verify_ok)
                binding.tvBackVerifyAction.setText(R.string.re_up)
            }
        }
        selfUploadSuccess.observe(this@DocumentReviewActivity) {
            binding.selfieUploadState.isVisible = it == true
            if (it == true) {
                binding.ivSelfVerifyStatus.setImageResource(R.mipmap.ic_verify_ok)
                binding.tvSelfVerifyAction.setText(R.string.re_up)
            }
        }
        frontUploadFailed.observe(this@DocumentReviewActivity) {
            if (it == true) {
                binding.frontUploadState.isVisible = true
                binding.ivFrontVerifyStatus.setImageResource(R.mipmap.ic_verify_fail)
                binding.tvFrontVerifyAction.setText(R.string.retry)
            }
        }
        backUploadFailed.observe(this@DocumentReviewActivity) {
            if (it == true) {
                binding.backUploadState.isVisible = true
                binding.ivBackVerifyStatus.setImageResource(R.mipmap.ic_verify_fail)
                binding.tvBackVerifyAction.setText(R.string.retry)
            }
        }
        selfUploadFailed.observe(this@DocumentReviewActivity) {
            if (it == true) {
                binding.selfieUploadState.isVisible = true
                binding.ivSelfVerifyStatus.setImageResource(R.mipmap.ic_verify_fail)
                binding.tvSelfVerifyAction.setText(R.string.retry)
            }
        }
        h5Live.observe(this@DocumentReviewActivity) {
            if (it.verifyUrl == null) {
                selfLauncher.launch(
                    Intent(
                        this@DocumentReviewActivity,
                        DFSilentLivenessActivity::class.java
                    )
                        .putExtra(
                            DFSilentLivenessActivity.KEY_DETECT_IMAGE_RESULT,
                            true
                        )
                )
                return@observe
            }
            h5Launcher.launch(
                ContentBrowserActivity.getIntent(this@DocumentReviewActivity, it.verifyUrl)
            )
        }
    }

    private fun resolveIdType(cardTypeName: String?) {
        if (cardTypeName.isNullOrBlank()) return
        personalVm.getEnums { options ->
            idType = options.idCardTypeV2
                ?.firstOrNull { it.info == cardTypeName }
                ?.state
        }
    }

    private fun updateIdImageSections() = with(binding) {
        val hasCardType = documentTypeField.getText().isNotBlank()
        val shouldShowCardImages = isCert || hasUploadedCardImages || hasCardType
        documentFrontSection.isVisible = shouldShowCardImages && vm.configResult.value?.KYC_FRONT != 0
        documentBackSection.isVisible = shouldShowCardImages && vm.configResult.value?.KYC_BACK != 0
        tvDocumentExampleAction.isVisible = !isCert
    }

    private fun getUri(outputFile: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            FileProvider.getUriForFile(
                this@DocumentReviewActivity,
                "$packageName.fileprovider",
                outputFile
            )
        else
            Uri.fromFile(outputFile)
    }

    private fun setIntents(intent: Intent, isFace: Boolean) {
        // Set camera facing based on the type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", isFace)
            intent.putExtra("camerafacing", if (isFace) "front" else "rear")
            intent.putExtra("previous_mode", if (isFace) "front" else "rear")
        }
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())

        intent.putExtra("android.intent.extras.CAMERA_FACING", if (isFace) 1 else 0)
    }

    private fun handleBackPressed() {
        if (binding.submissionActionContainer.isVisible) {
            val list = authConfigList.filterNot { it1 -> it1.isBlank() }
            val step = list.size - max(0, list.indexOf("KYC"))
            showConfirmDialog(
                desc = String.format(
                    getString(R.string.auth_exit_confirm),
                    step.toString()
                ),
                cancel = getString(R.string.give_up),
                ok = getString(R.string.continue_str),
                highLight = step.toString(),
                cancelButtonSurfaceSecondary = true,
                cancelAction = {
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoKyc,
                            act = ACT_clickBack,
                        )
                    )
                    finish()
                }
            ) {
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoKyc,
                        act = ACT_clickContinue,
                    )
                )
            }
        } else {
            finish()
        }
    }
}
