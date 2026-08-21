package com.velora.portal.feature.onboarding.presentation.profile

import android.Manifest
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.velora.portal.application.MainApplication
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.ACT_clickNext
import com.velora.portal.platform.common.data.ACT_in
import com.velora.portal.platform.common.data.ACT_inputAddressEnd
import com.velora.portal.platform.common.data.ACT_inputAddressStart
import com.velora.portal.platform.common.data.ACT_inputEducationEnd
import com.velora.portal.platform.common.data.ACT_inputEducationStart
import com.velora.portal.platform.common.data.ACT_inputIDCardNumberEnd
import com.velora.portal.platform.common.data.ACT_inputIDCardNumberStart
import com.velora.portal.platform.common.data.ACT_inputMaritalStateEnd
import com.velora.portal.platform.common.data.ACT_inputMaritalStateStart
import com.velora.portal.platform.common.data.ACT_inputNameEnd
import com.velora.portal.platform.common.data.ACT_inputNameStart
import com.velora.portal.platform.common.data.ACT_inputSalaryEnd
import com.velora.portal.platform.common.data.ACT_inputSalaryStart
import com.velora.portal.platform.common.data.ACT_selectDateEnd
import com.velora.portal.platform.common.data.ACT_selectDateStart
import com.velora.portal.platform.common.data.ACT_selectIndustryEnd
import com.velora.portal.platform.common.data.ACT_selectIndustryStart
import com.velora.portal.platform.common.data.ACT_selectProfessionEnd
import com.velora.portal.platform.common.data.ACT_selectProfessionStart
import com.velora.portal.platform.common.data.ACT_selectReasonOfLoanEnd
import com.velora.portal.platform.common.data.ACT_selectReasonOfLoanStart
import com.velora.portal.platform.common.data.ACT_selectWorkTimeEnd
import com.velora.portal.platform.common.data.ACT_selectWorkTimeStart
import com.velora.portal.platform.common.data.PageInfoPersonal
import com.velora.portal.platform.common.data.PagePrivacy
import com.velora.portal.platform.common.data.authConfigList
import com.velora.portal.platform.common.data.bean.ApiRequest
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.databinding.ActivityApplicantDetailsBinding
import com.velora.portal.feature.onboarding.presentation.AuthStatusViewModel
import com.velora.portal.feature.onboarding.presentation.routeToNextAuthStep
import com.velora.portal.platform.design.extension.resetScale
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.feature.onboarding.presentation.dialog.showAddressPickerDialog
import com.velora.portal.platform.design.dialog.showConfirmDialog
import com.velora.portal.platform.design.dialog.showDatePickerDialog
import com.velora.portal.platform.design.dialog.showOptionPickerDialog
import com.velora.portal.platform.common.util.PERSON_INFO_COMMIT
import com.velora.portal.platform.common.util.PERSON_INFO_PAGE
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.PermissionScenario
import com.velora.portal.platform.common.util.text.isAdult
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.common.util.text.toDmyDateString
import com.velora.portal.platform.common.util.text.toYmdDateString
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.platform.common.util.viewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class ApplicantDetailsActivity :
    BaseActivity<ActivityApplicantDetailsBinding>() {

    override val binding by viewBinding(ActivityApplicantDetailsBinding::inflate)
    private val isCert by lazy {
        intent.getBooleanExtra("isCert", false)
    }
    private val vm by viewModels<ApplicantDetailsViewModel>()
    private val homeVm by viewModels<AuthStatusViewModel>()
    private val debounceTime = 500L  // treat as input finished after 500ms idle
    private var shouldShowBottomAction = false

    private var startSalaryTime: Long = 0L
    private var salaryJob: Job? = null
    private var startNameTime: Long = 0L
    private var nameJob: Job? = null
    private var startIDTime: Long = 0L
    private var idJob: Job? = null

    override fun shouldDismissKeyboardOnOutsideTouch(ev: MotionEvent): Boolean {
        val bottomActionBounds = Rect()
        binding.applicantSubmitBar.getGlobalVisibleRect(bottomActionBounds)
        return !bottomActionBounds.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }

    override fun initView() {
        prepareApplicantProfileScreen()
        bindProfileTextFields()
        bindProfileOptionSelectors()
        configureProfileActions()
    }

    private fun prepareApplicantProfileScreen() = with(binding) {
        shouldShowBottomAction = !isCert
        applicantSubmitBar.isVisible = shouldShowBottomAction
        configureFormEditing(isEditable = !isCert)
        trackEvent(PERSON_INFO_PAGE)
        vm.submitTrackingEvent(
            TrackBean(
                p = PageInfoPersonal,
                act = ACT_in,
            )
        )
        applicantDetailsHeader.setNavigationAction { confirmProfileExit() }
        registerTrackedBackHandler(vm) {
            confirmProfileExit()
        }
        applicantDetailsHeader.setAction(
            "${authConfigList.indexOf("ID") + 1}/${authConfigList.size}"
        )
        pageContent.isVisible = false
        pageState.showLoading()
        vm.getPersonalInfo {
            pageContent.isVisible = false
            pageState.showError()
        }
    }

    private fun configureFormEditing(isEditable: Boolean) = with(binding) {
        listOf(
            fieldFamilyName,
            fieldGivenName,
            fieldGenderSelection,
            fieldBirthDate,
            fieldIdentityNumber,
            fieldEducationLevel,
            fieldEmploymentIndustry,
            fieldJobRole,
            fieldEmploymentDuration,
            fieldLoanPurpose,
            fieldMonthlyIncome,
            fieldMaritalStatus,
            fieldResidentialRegion,
            fieldStreetAddress,
        ).forEach { field ->
            field.setEnableEdit(isEditable)
        }
    }

    private fun bindProfileTextFields() = with(binding) {
        fieldFamilyName.getEditText().doOnTextChanged { _, _, _, _ ->
            val now = System.currentTimeMillis()
            // 1. first input → record start time
            if (startNameTime == 0L) {
                startNameTime = now
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputNameStart,
                        result = now.toString()
                    )
                )
            }
            // 2. typing → reset end timer
            nameJob?.cancel()
            nameJob = lifecycleScope.launch {
                delay(debounceTime)
                // 3. user stopped typing → record end time
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputNameEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
            }
        }
        fieldGivenName.getEditText().doOnTextChanged { _, _, _, _ ->
            val now = System.currentTimeMillis()
            // 1. first input → record start time
            if (startNameTime == 0L) {
                startNameTime = now
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputNameStart,
                        result = now.toString()
                    )
                )
            }
            // 2. typing → reset end timer
            nameJob?.cancel()
            nameJob = lifecycleScope.launch {
                delay(debounceTime)
                // 3. user stopped typing → record end time
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputNameEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
            }
        }
        fieldGivenName.getEditText().doAfterTextChanged {
            fieldGivenName.hideError()
        }
        fieldIdentityNumber.getEditText().doOnTextChanged { _, _, _, _ ->
            val now = System.currentTimeMillis()
            // 1. first input → record start time
            if (startIDTime == 0L) {
                startIDTime = now
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputIDCardNumberStart,
                        result = now.toString()
                    )
                )
            }
            // 2. typing → reset end timer
            idJob?.cancel()
            idJob = lifecycleScope.launch {
                delay(debounceTime)
                // 3. user stopped typing → record end time
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputIDCardNumberEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
            }
        }
        fieldMonthlyIncome.getEditText().doAfterTextChanged {
            it?.let {
                var input = it.toString()
                // remove leading zeros, but keep "0" itself
                if (input.length > 1 && input.startsWith("0") && !input.startsWith("0.")) {
                    input = input.replaceFirst("^0+".toRegex(), "")
                    if (input.isEmpty()) input = "0"
                    fieldMonthlyIncome.setText(input)
                    fieldMonthlyIncome.getEditText().setSelection(input.length)
                }
                fieldMonthlyIncome.hideError()
            }
        }
        fieldMonthlyIncome.getEditText().doOnTextChanged { _, _, _, _ ->
            val now = System.currentTimeMillis()
            // 1. first input → record start time
            if (startSalaryTime == 0L) {
                startSalaryTime = now
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputSalaryStart,
                        result = now.toString()
                    )
                )
            }
            // 2. typing → reset end timer
            salaryJob?.cancel()
            salaryJob = lifecycleScope.launch {
                delay(debounceTime)
                // 3. user stopped typing → record end time
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputSalaryEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
            }
        }
    }

    private fun bindProfileOptionSelectors() = with(binding) {
        fieldGenderSelection.setOnClick {
            vm.getEnums {
                val genderList = it.gender ?: arrayListOf()
                showOptionPickerDialog(
                    genderList.indexOfFirst { it1 -> it1.info == fieldGenderSelection.getText() },
                    genderList
                ) { index ->
                    fieldGenderSelection.setText(genderList[index].info)
                    fieldGenderSelection.hideError()
                    genderStatus = it.gender?.get(index)?.state
                }
            }
        }
        fieldBirthDate.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_selectDateStart,
                    result = System.currentTimeMillis().toString()
                )
            )
            showDatePickerDialog { dateStr ->
                fieldBirthDate.setText(dateStr)
                fieldBirthDate.hideError()
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_selectDateEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
            }
        }
        fieldEducationLevel.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_inputEducationStart,
                    result = System.currentTimeMillis().toString()
                )
            )
            vm.getEnums {
                val genderList = it.education ?: arrayListOf()
                showOptionPickerDialog(
                    genderList.indexOfFirst { it1 -> fieldEducationLevel.getText() == it1.info },
                    genderList
                ) { index ->
                    fieldEducationLevel.setText(genderList[index].info)
                    fieldEducationLevel.hideError()
                    eduStatus = it.education?.get(index)?.state
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoPersonal,
                            act = ACT_inputEducationEnd,
                            result = System.currentTimeMillis().toString()
                        )
                    )
                }
            }
        }
        fieldEmploymentIndustry.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_selectIndustryStart,
                    result = System.currentTimeMillis().toString(),
                ),
            )
            vm.getWorkInfoOptions {
                val options = it.industry.orEmpty()
                showOptionPickerDialog(
                    options.indexOfFirst { option -> option.info == fieldEmploymentIndustry.getText() },
                    options,
                ) { index ->
                    fieldEmploymentIndustry.setText(options[index].info)
                    fieldEmploymentIndustry.hideError()
                    industryStatus = options[index].state
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoPersonal,
                            act = ACT_selectIndustryEnd,
                            result = System.currentTimeMillis().toString(),
                        ),
                    )
                }
            }
        }
        fieldJobRole.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_selectProfessionStart,
                    result = System.currentTimeMillis().toString(),
                ),
            )
            vm.getWorkInfoOptions {
                val options = it.jobnature.orEmpty()
                showOptionPickerDialog(
                    options.indexOfFirst { option -> option.info == fieldJobRole.getText() },
                    options,
                ) { index ->
                    fieldJobRole.setText(options[index].info)
                    fieldJobRole.hideError()
                    professionStatus = options[index].state
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoPersonal,
                            act = ACT_selectProfessionEnd,
                            result = System.currentTimeMillis().toString(),
                        ),
                    )
                }
            }
        }
        fieldEmploymentDuration.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_selectWorkTimeStart,
                    result = System.currentTimeMillis().toString(),
                ),
            )
            vm.getEnums {
                val options = it.workTime.orEmpty()
                showOptionPickerDialog(
                    options.indexOfFirst { option -> option.info == fieldEmploymentDuration.getText() },
                    options,
                ) { index ->
                    fieldEmploymentDuration.setText(options[index].info)
                    fieldEmploymentDuration.hideError()
                    workTimeStatus = options[index].state
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoPersonal,
                            act = ACT_selectWorkTimeEnd,
                            result = System.currentTimeMillis().toString(),
                        ),
                    )
                }
            }
        }
        fieldLoanPurpose.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_selectReasonOfLoanStart,
                    result = System.currentTimeMillis().toString(),
                ),
            )
            vm.getEnums {
                val options = it.purpose.orEmpty()
                showOptionPickerDialog(
                    options.indexOfFirst { option -> option.info == fieldLoanPurpose.getText() },
                    options,
                ) { index ->
                    fieldLoanPurpose.setText(options[index].info)
                    fieldLoanPurpose.hideError()
                    reasonStatus = options[index].state
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoPersonal,
                            act = ACT_selectReasonOfLoanEnd,
                            result = System.currentTimeMillis().toString(),
                        ),
                    )
                }
            }
        }
        fieldMaritalStatus.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_inputMaritalStateStart,
                    result = System.currentTimeMillis().toString()
                )
            )
            vm.getEnums {
                val genderList = it.maritalStatus ?: arrayListOf()
                showOptionPickerDialog(
                    genderList.indexOfFirst { it1 -> it1.info == fieldMaritalStatus.getText() },
                    genderList
                ) { index ->
                    fieldMaritalStatus.setText(genderList[index].info)
                    fieldMaritalStatus.hideError()
                    marStatus = it.maritalStatus?.get(index)?.state
                    vm.submitTrackingEvent(
                        TrackBean(
                            p = PageInfoPersonal,
                            act = ACT_inputMaritalStateEnd,
                            result = System.currentTimeMillis().toString()
                        )
                    )
                }
            }
        }
        fieldResidentialRegion.setOnClick {
            vm.submitTrackingEvent(
                TrackBean(
                    p = PageInfoPersonal,
                    act = ACT_inputAddressStart,
                    result = System.currentTimeMillis().toString()
                )
            )
            provinceId = null
            cityId = null
            areaId = null
            fieldResidentialRegion.setText("")
            showAddressPickerDialog(vm) { it, pId, cId, aId ->
                fieldResidentialRegion.setText(it)
                fieldResidentialRegion.hideError()
                provinceId = pId
                cityId = cId
                areaId = aId
//                LogUtil.d("pId$pId|$cityId|$areaId")
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_inputAddressEnd,
                        result = System.currentTimeMillis().toString()
                    )
                )
            }
        }
    }

    private fun configureProfileActions() = with(binding) {
        applicantDetailsHeader.showAction(!isCert)
        if (!isCert) {
            btnSubmitApplicantDetails.resetScale()
        }
        btnSubmitApplicantDetails.singleClick {
            if (fieldFamilyName.getText().isBlank()) {
                fieldFamilyName.showError()
                scrollToInvalidField(fieldFamilyName)
                return@singleClick
            }
            if (fieldGivenName.getText().isBlank()) {
                fieldGivenName.showError()
                scrollToInvalidField(fieldGivenName)
                return@singleClick
            }
            if (fieldGenderSelection.getText().isBlank()) {
                fieldGenderSelection.showError()
                scrollToInvalidField(fieldGenderSelection)
                return@singleClick
            }
            val birthDate = fieldBirthDate.getText()
            if (birthDate.isBlank()) {
                fieldBirthDate.showError()
                scrollToInvalidField(fieldBirthDate)
                return@singleClick
            }
            if (!birthDate.isAdult()) {
                fieldBirthDate.showError()
                scrollToInvalidField(fieldBirthDate)
                getString(R.string.under_18).showToastMessage()
                return@singleClick
            }
            if (fieldIdentityNumber.getText().isBlank()) {
                getString(R.string.id_number_error).showToastMessage()
                fieldIdentityNumber.showError()
                scrollToInvalidField(fieldIdentityNumber)
                return@singleClick
            }
            if (fieldEducationLevel.getText().isBlank()) {
                fieldEducationLevel.showError()
                scrollToInvalidField(fieldEducationLevel)
                return@singleClick
            }
            if (fieldEmploymentIndustry.getText().isBlank()) {
                fieldEmploymentIndustry.showError()
                scrollToInvalidField(fieldEmploymentIndustry)
                return@singleClick
            }
            if (fieldJobRole.getText().isBlank()) {
                fieldJobRole.showError()
                scrollToInvalidField(fieldJobRole)
                return@singleClick
            }
            if (fieldEmploymentDuration.getText().isBlank()) {
                fieldEmploymentDuration.showError()
                scrollToInvalidField(fieldEmploymentDuration)
                return@singleClick
            }
            if (fieldLoanPurpose.getText().isBlank()) {
                fieldLoanPurpose.showError()
                scrollToInvalidField(fieldLoanPurpose)
                return@singleClick
            }
            if (fieldMonthlyIncome.getText().isBlank()) {
                fieldMonthlyIncome.showError()
                scrollToInvalidField(fieldMonthlyIncome)
                return@singleClick
            }
            if (fieldMaritalStatus.getText().isBlank()) {
                fieldMaritalStatus.showError()
                scrollToInvalidField(fieldMaritalStatus)
                return@singleClick
            }
            if (provinceId == null || cityId == null || areaId == null) {
                fieldResidentialRegion.showError()
                scrollToInvalidField(fieldResidentialRegion)
                return@singleClick
            }
            if (fieldStreetAddress.getText().isBlank()) {
                fieldStreetAddress.showError()
                scrollToInvalidField(fieldStreetAddress)
                return@singleClick
            }
            PermissionCoordinator.request(
                this@ApplicantDetailsActivity,
                PermissionScenario.DEVICE_RISK,
                onDenied = { _, pList ->
                    vm.submitTrackingEvents(pList.map { it1 ->
                        TrackBean(
                            p = PagePrivacy,
                            act = when (it1) {
                                Manifest.permission.ACCESS_COARSE_LOCATION -> "gps"
                                Manifest.permission.READ_PHONE_STATE -> "device"
                                else -> "notification"
                            },
                            result = "reject"
                        )
                    })
                }) {
                vm.submitTrackingEvent(
                    TrackBean(
                        p = PageInfoPersonal,
                        act = ACT_clickNext,
                    )
                )
                vm.submitTrackingEvents(it.map { it1 ->
                    TrackBean(
                        p = PagePrivacy,
                        act = when (it1) {
                            Manifest.permission.ACCESS_COARSE_LOCATION -> "gps"
                            Manifest.permission.READ_PHONE_STATE -> "device"
                            else -> "notification"
                        },
                        result = "agree"
                    )
                })
                MainApplication.appViewModel.postRiskInfo(PageInfoPersonal) { isSuccess ->
                    if (isSuccess) {
                        submit()
                    }
                }
            }
        }
        pageState.setOnRetryClickListener {
            pageContent.isVisible = false
            pageState.showLoading()
            vm.getPersonalInfo {
                pageContent.isVisible = false
                pageState.showError()
            }
        }
    }

    /** Keeps the first invalid field fully visible above the keyboard-attached action area. */
    private fun scrollToInvalidField(target: View) = with(binding) {
        applicantFormScroll.post {
            val targetBounds = Rect().also(target::getDrawingRect)
            applicantFormScroll.offsetDescendantRectToMyCoords(target, targetBounds)

            val scrollLocation = IntArray(2)
            val actionLocation = IntArray(2)
            applicantFormScroll.getLocationOnScreen(scrollLocation)
            applicantSubmitBar.getLocationOnScreen(actionLocation)

            val spacing = resources.getDimensionPixelSize(R.dimen.dp_12)
            val viewportTop = applicantFormScroll.paddingTop + spacing
            val scrollBottom = applicantFormScroll.height - applicantFormScroll.paddingBottom - spacing
            val actionTop = actionLocation[1] - scrollLocation[1] - spacing
            val viewportBottom = minOf(scrollBottom, actionTop)
            if (viewportBottom <= viewportTop) return@post

            // targetBounds uses content coordinates; include scrollY in the visible bounds too.
            val visibleTop = applicantFormScroll.scrollY + viewportTop
            val visibleBottom = applicantFormScroll.scrollY + viewportBottom

            val availableHeight = visibleBottom - visibleTop
            val scrollDelta = when {
                targetBounds.height() > availableHeight -> targetBounds.top - visibleTop
                targetBounds.top < visibleTop -> targetBounds.top - visibleTop
                targetBounds.bottom > visibleBottom -> targetBounds.bottom - visibleBottom
                else -> 0
            }
            if (scrollDelta != 0) {
                applicantFormScroll.smoothScrollBy(0, scrollDelta)
            }
        }
    }

    private fun submit() {
        trackEvent(PERSON_INFO_COMMIT)
        vm.submitPersonalInfo(
            ApiRequest(
                education = eduStatus.toString(),
                sex = genderStatus.toString(),
                marryState = marStatus.toString(),
                lastName = binding.fieldFamilyName.getText(),
                firstName = binding.fieldGivenName.getText(),
                cardNo = binding.fieldIdentityNumber.getText(),
                birthDate = binding.fieldBirthDate.getText().toYmdDateString(),
                province = provinceId.toString(),
                address = binding.fieldStreetAddress.getText(),
                region = areaId.toString(),
                city = cityId.toString(),
                salary = binding.fieldMonthlyIncome.getText(),
                jobNature = professionStatus.toString(),
                industry = industryStatus.toString(),
                loanPurpose = reasonStatus.toString(),
                workTime = workTimeStatus.toString(),
//                                userCommunicationRecordStr = Gson().toJson(getCallLog()).encodeBase64()
            )
        )
    }

    private var genderStatus: Int? = null
    private var eduStatus: Int? = null
    private var industryStatus: Int? = null
    private var professionStatus: Int? = null
    private var workTimeStatus: Int? = null
    private var reasonStatus: Int? = null
    private var marStatus: Int? = null
    private var provinceId: Long? = null
    private var cityId: Long? = null
    private var areaId: Long? = null

    override fun initObserve() = with(vm) {
        super.initObserve()
        personalResult.observe(this@ApplicantDetailsActivity) {
            binding.apply {
                pageContent.isVisible = true
                pageState.hide()
                it?.let {
                    fieldFamilyName.setText(it.lastName)
                    fieldGivenName.setText(it.firstName)
                    fieldIdentityNumber.setText(it.cardNo)
                    fieldGenderSelection.setText(it.sexStr)
                    fieldBirthDate.setText(it.birthDateStr?.toDmyDateString())
                    fieldEducationLevel.setText(it.educationStr)
                    fieldEmploymentIndustry.setText(it.industry)
                    fieldJobRole.setText(it.jobNature)
                    fieldEmploymentDuration.setText(it.workTime)
                    fieldLoanPurpose.setText(it.purposeStr)
                    fieldMaritalStatus.setText(it.marryStateStr)
                    fieldStreetAddress.setText(it.currentAddress)
                    fieldMonthlyIncome.setText(if (it.salary == null) "" else it.salary.toString())
                    if (it.provinceStr != null) {
                        fieldResidentialRegion.setText(
                            String.format(
                                "%s/%s/%s", it.provinceStr, it.cityStr, it.regionStr
                            )
                        )
                    }
                    genderStatus = it.sex
                    eduStatus = it.education
                    marStatus = it.marryState
                    provinceId = it.province
                    cityId = it.city
                    areaId = it.region
                    restoreSelectionStates()
                }
            }
        }
        homeVm.userAuthStatusResult.observe(this@ApplicantDetailsActivity) {
            it?.routeToNextAuthStep(this@ApplicantDetailsActivity)
            finish()
        }
        submitResult.observe(this@ApplicantDetailsActivity) {
            homeVm.getUserAuthStatus()
        }
    }

    private fun restoreSelectionStates() {
        vm.getEnums { options ->
            eduStatus = options.education.orEmpty()
                .firstOrNull { it.info == binding.fieldEducationLevel.getText() }
                ?.state ?: eduStatus
            workTimeStatus = options.workTime.orEmpty()
                .firstOrNull { it.info == binding.fieldEmploymentDuration.getText() }
                ?.state ?: workTimeStatus
            reasonStatus = options.purpose.orEmpty()
                .firstOrNull { it.info == binding.fieldLoanPurpose.getText() }
                ?.state ?: reasonStatus
        }
        vm.getWorkInfoOptions { options ->
            industryStatus = options.industry.orEmpty()
                .firstOrNull { it.info == binding.fieldEmploymentIndustry.getText() }
                ?.state ?: industryStatus
            professionStatus = options.jobnature.orEmpty()
                .firstOrNull { it.info == binding.fieldJobRole.getText() }
                ?.state ?: professionStatus
        }
    }

    private fun confirmProfileExit() {
        if (shouldShowBottomAction) {
            val list = authConfigList.filterNot { it1 -> it1.isBlank() }
            val step =
                list.size - max(0, list.indexOf("ID"))
            showConfirmDialog(
                desc = String.format(
                    getString(R.string.auth_exit_confirm),
                    step.toString()
                ),
                cancel = getString(R.string.give_up),
                cancelButtonSurfaceSecondary = true,
                ok = getString(R.string.continue_str),
                highLight = step.toString(),
                cancelAction = { finish() }
            ) {}
        } else {
            finish()
        }
    }
}
