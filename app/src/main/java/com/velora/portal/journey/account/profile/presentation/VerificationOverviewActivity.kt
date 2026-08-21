package com.velora.portal.journey.account.profile.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenVerificationOverviewBinding
import com.velora.portal.platform.common.data.authConfigList
import com.velora.portal.domain.customer.model.VerificationOptionResponse
import com.velora.portal.domain.customer.model.VerificationProgressResponse
import com.velora.portal.journey.access.presentation.contact.ContactPayoutActivity
import com.velora.portal.journey.access.presentation.kyc.IdentityCheckActivity
import com.velora.portal.journey.access.presentation.profile.BorrowerProfileActivity
import com.velora.portal.journey.account.profile.presentation.adapter.VerificationRequirementAdapter
import com.velora.portal.journey.access.presentation.AuthStatusViewModel
import com.velora.portal.platform.common.util.PROFILE_PAGE
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.trackEvent
import com.velora.portal.platform.common.util.viewBinding

class VerificationOverviewActivity : BaseActivity<ScreenVerificationOverviewBinding>() {

    override val binding by viewBinding(ScreenVerificationOverviewBinding::inflate)

    private val vm by viewModels<AuthStatusViewModel>()

    private val authAdapter by lazy {
        VerificationRequirementAdapter().apply {
            setOnItemClickListener { item, _ ->
                when (if (item.isCertified) item.title else items.first { !it.isCertified }.title) {
                    getString(R.string.kyc_certification) -> {
                        context.start<IdentityCheckActivity> {
                            putExtra("isCert", item.isCertified)
                        }
                    }

                    getString(R.string.personal_info) -> {
                        context.start<BorrowerProfileActivity> {
                            putExtra("isCert", item.isCertified)
                        }
                    }

                    else -> {
                        context.start<ContactPayoutActivity> {
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
        userAuthStatusState.observe(this@VerificationOverviewActivity) { state ->
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
