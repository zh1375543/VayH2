package com.velora.portal.moneyflow.fg

import android.content.Intent
import androidx.fragment.app.viewModels
import com.velora.portal.R
import com.velora.portal.platform.common.data.PRIVACY_POLICY
import com.velora.portal.platform.design.base.BaseFragment
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.databinding.FragmentMemberSpaceBinding
import com.velora.portal.journey.lending.dashboard.presentation.VisitorPortalViewModel
import com.velora.portal.journey.lending.dashboard.presentation.dialog.showContactUsDialog
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.moneyflow.ac.AccountSettingsActivity
import com.velora.portal.platform.common.util.ExternalActionLauncher
import com.velora.portal.platform.common.util.maskPhoneNumber
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.browser.presentation.ContentBrowserActivity
import kotlin.getValue

/** Account page for the calculation experience. */
class MemberSpaceFragment : BaseFragment<FragmentMemberSpaceBinding>(
    R.layout.fragment_member_space
) {
    override val binding by viewBinding(FragmentMemberSpaceBinding::bind)
    private val vm by viewModels<VisitorPortalViewModel>()

    override fun initView() {
        configureAccountHeader()
        bindAccountActions()
        bindSupportActions()
    }

    private fun configureAccountHeader() = with(binding) {
        applyTopInset(accountScroll)
        tvRatingPrompt.text = getString(
            R.string.account_rating_prompt,
            requireContext().applicationInfo.loadLabel(requireContext().packageManager),
        )
    }

    private fun bindAccountActions() = with(binding) {
        tvRate.singleClick {
            val opened = context?.let {
                ExternalActionLauncher.openRatingPage(it)
            } ?: false
            if (!opened) {
                getString(R.string.unable_open_google).showToastMessage()
            }
        }
        tvShareApp.singleClick {
            shareApp()
        }
        tvSettings.singleClick {
            context?.start<AccountSettingsActivity>()
        }
    }

    private fun bindSupportActions() = with(binding) {
        tvHelpCenter.singleClick {
            vm.getUnAuthData(true)
        }
        tvPrivacy.singleClick {
            ContentBrowserActivity.launch(
                requireContext(),
                getString(R.string.privacy_policy),
                PRIVACY_POLICY,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvPhone.text = SessionStore.loginInfo?.phone.orEmpty().maskPhoneNumber()
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=${requireContext().packageName}"
            )
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
    }

    override fun initObserve() = with(vm) {
        result.observe(this@MemberSpaceFragment) {
            it?.let { requireContext().showContactUsDialog(it) }
        }
    }
}
