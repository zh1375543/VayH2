package com.velora.portal.journey.account.profile.presentation

import androidx.fragment.app.viewModels
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseFragment
import com.velora.portal.platform.common.data.ACT_inMy
import com.velora.portal.platform.common.data.PageMine
import com.velora.portal.platform.common.data.PRIVACY_POLICY
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.databinding.FragmentProfileCenterBinding
import com.velora.portal.journey.lending.catalog.presentation.LoanDashboardViewModel
import com.velora.portal.journey.servicing.records.presentation.LoanHistoryActivity
import com.velora.portal.journey.servicing.checkout.presentation.BulkRepaymentActivity
import com.velora.portal.journey.servicing.checkout.presentation.dialog.createPaybackDialog
import com.velora.portal.platform.browser.presentation.ContentBrowserActivity
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.journey.account.accounts.presentation.PayoutAccountListActivity

class ProfileCenterFragment : BaseFragment<FragmentProfileCenterBinding>(
    R.layout.fragment_profile_center
) {
    override val binding by viewBinding(FragmentProfileCenterBinding::bind)

    private val vm by viewModels<LoanDashboardViewModel>()

    private val paybackDialog by lazy {
        requireContext().createPaybackDialog()
    }

    override fun initView() = with(binding) {

        tvContactUs.singleClick {
            context?.start<CustomerSupportActivity>()
        }
        tvAboutUs.singleClick {
            context?.start<AppInfoActivity>()
        }
        tvSettings.singleClick {
            context?.start<AppSettingsActivity>()
        }
        tvPolicy.singleClick {
            ContentBrowserActivity.Companion.launch(
                it.context,
                getString(R.string.privacy_policy),
                PRIVACY_POLICY
            )
        }
        tvAccount.singleClick {
            it.context.start<PayoutAccountListActivity>()
        }
        tvOrder.singleClick {
            context?.start<LoanHistoryActivity>()
        }
        tvPayBack.singleClick {
            vm.getAuthData(true)
        }
        tvCert.singleClick {
            it.context.start<VerificationOverviewActivity>()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvPhone.text = SessionStore.loginInfo?.phone
//        binding.tvPhone.setClickableTextWithScale(
//            String.format(getString(R.string.welcome) + "\n" + SessionStore.loginInfo?.phone),
//            SessionStore.loginInfo?.phone.orEmpty(),
//            binding.root.context.resolveColorCompat(R.color.C_492E0D)
//        )
        vm.submitTrackingEvent(
            TrackBean(
                p = PageMine,
                act = ACT_inMy,
                result = System.currentTimeMillis().toString()
            )
        )
    }

    override fun initObserve() = with(vm) {
        authResult.observe(this@ProfileCenterFragment) {
            if (it?.showMultipleRepaySign == 1) {
                context?.start<BulkRepaymentActivity>()
            } else {
                paybackDialog.show()
            }
        }
    }
}
