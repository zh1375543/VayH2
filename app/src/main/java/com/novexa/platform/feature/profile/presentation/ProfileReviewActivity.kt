package com.novexa.platform.feature.profile.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityProfileReviewBinding
import com.novexa.platform.core.common.data.authConfigList
import com.novexa.platform.feature.onboarding.model.VerificationOptionResponse
import com.novexa.platform.feature.onboarding.model.VerificationProgressResponse
import com.novexa.platform.feature.onboarding.presentation.contact.PaymentDetailsActivity
import com.novexa.platform.feature.onboarding.presentation.kyc.DocumentReviewActivity
import com.novexa.platform.feature.onboarding.presentation.profile.ApplicantDetailsActivity
import com.novexa.platform.feature.profile.presentation.adapter.VerificationRequirementAdapter
import com.novexa.platform.feature.onboarding.presentation.AuthStatusViewModel
import com.novexa.platform.core.common.util.PROFILE_PAGE
import com.novexa.platform.core.common.util.PageLoadState
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.trackEvent
import com.novexa.platform.core.common.util.viewBinding

class ProfileReviewActivity : BaseActivity<ActivityProfileReviewBinding>() {

    override val binding by viewBinding(ActivityProfileReviewBinding::inflate)

    private val vm by viewModels<AuthStatusViewModel>()

    private val authAdapter by lazy {
        VerificationRequirementAdapter().apply {
            setOnItemClickListener { item, _ ->
                when (if (item.isCertified) item.title else items.first { !it.isCertified }.title) {
                    getString(R.string.kyc_certification) -> {
                        context.start<DocumentReviewActivity> {
                            putExtra("isCert", item.isCertified)
                        }
                    }

                    getString(R.string.personal_info) -> {
                        context.start<ApplicantDetailsActivity> {
                            putExtra("isCert", item.isCertified)
                        }
                    }

                    else -> {
                        context.start<PaymentDetailsActivity> {
                            putExtra("isCert", item.isCertified)
                        }
                    }
                }
            }
        }
    }

    override fun initView() = with(binding) {
        trackEvent(PROFILE_PAGE)
        rvAuth.adapter = authAdapter
        pageState.setOnRetryClickListener {
            vm.getUserAuthStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.getUserAuthStatus()
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        userAuthStatusState.observe(this@ProfileReviewActivity) { state ->
            render(state)
        }
    }

    private fun render(state: PageLoadState<VerificationProgressResponse>) = with(binding) {
        rvAuth.isVisible = state is PageLoadState.Content
        when (state) {
            PageLoadState.Loading -> pageState.showLoading()
            PageLoadState.Error -> pageState.showError()
            PageLoadState.Empty -> pageState.showEmpty()

            is PageLoadState.Content -> {
                val entries = authConfigList.map { type ->
                    val (titleRes, iconRes, checker) =
                        authMap[type.uppercase()] ?: authMap.getValue("TELECOM")

                    VerificationOptionResponse(
                        title = getString(titleRes),
                        isCertified = checker(state.data),
                        src = iconRes,
                    )
                }
                if (entries.isEmpty()) {
                    rvAuth.isVisible = false
                    pageState.showEmpty()
                } else {
                    if (authAdapter.items != entries) {
                        authAdapter.submitItems(entries)
                    }
                    pageState.hide()
                }
            }
        }
    }

    private val authMap by lazy {
        mapOf(
            "KYC" to Triple(
                R.string.kyc_certification,
                R.mipmap.icon_cert_kyc
            ) { bean: VerificationProgressResponse ->
                bean.kycState == "30"
            },
            "ID" to Triple(
                R.string.personal_info,
                R.mipmap.icon_cert_personal
            ) { bean ->
                bean.idState == "30"
            },
            "BANK" to Triple(
                R.string.contact_info,
                R.mipmap.icon_cert_contact
            ) { bean ->
                bean.bankCardState == "30"
            },
            "TELECOM" to Triple(
                R.string.service_provider,
                R.mipmap.ic_cert_service
            ) { bean ->
                bean.telecomPermissionState == "30"
            }
        )
    }
}
