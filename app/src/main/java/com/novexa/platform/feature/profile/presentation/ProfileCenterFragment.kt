package com.novexa.platform.feature.profile.presentation

import androidx.fragment.app.viewModels
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseFragment
import com.novexa.platform.core.common.data.ACT_inMy
import com.novexa.platform.core.common.data.PageMine
import com.novexa.platform.core.common.data.PRIVACY_POLICY
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.databinding.FragmentProfileCenterBinding
import com.novexa.platform.feature.catalog.presentation.LoanDashboardViewModel
import com.novexa.platform.feature.records.presentation.RecordHistoryActivity
import com.novexa.platform.feature.checkout.presentation.BatchCheckoutActivity
import com.novexa.platform.feature.checkout.presentation.dialog.createPaybackDialog
import com.novexa.platform.feature.content.presentation.ContentBrowserActivity
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.feature.accounts.presentation.LinkedAccountListActivity

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
            context?.start<ContactsActivity>()
        }
        tvAboutUs.singleClick {
            context?.start<AboutActivity>()
        }
        tvSettings.singleClick {
            context?.start<SetActivity>()
        }
        tvPolicy.singleClick {
            ContentBrowserActivity.Companion.launch(
                it.context,
                getString(R.string.privacy_policy),
                PRIVACY_POLICY
            )
        }
        tvAccount.singleClick {
            it.context.start<LinkedAccountListActivity>()
        }
        tvOrder.singleClick {
            context?.start<RecordHistoryActivity>()
        }
        tvPayBack.singleClick {
            vm.getAuthData(true)
        }
        tvCert.singleClick {
            it.context.start<ProfileReviewActivity>()
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
                context?.start<BatchCheckoutActivity>()
            } else {
                paybackDialog.show()
            }
        }
    }
}
