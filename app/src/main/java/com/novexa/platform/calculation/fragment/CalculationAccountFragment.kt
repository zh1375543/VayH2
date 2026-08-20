package com.novexa.platform.calculation.fragment

import android.content.Intent
import com.novexa.platform.R
import com.novexa.platform.core.common.data.PRIVACY_POLICY
import com.novexa.platform.core.ui.base.BaseFragment
import com.novexa.platform.core.session.SessionStore
import com.novexa.platform.databinding.SidepageMineFragmentBinding
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.calculation.activitiy.AccountSettingsActivity
import com.novexa.platform.calculation.activitiy.HelpCenterActivity
import com.novexa.platform.core.common.util.ExternalActionLauncher
import com.novexa.platform.core.common.util.maskPhoneNumber
import com.novexa.platform.core.common.util.showToastMessage
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.feature.content.presentation.ContentBrowserActivity

/** Account page for the calculation experience. */
class CalculationAccountFragment : BaseFragment<SidepageMineFragmentBinding>(
    R.layout.sidepage_mine_fragment
) {
    override val binding by viewBinding(SidepageMineFragmentBinding::bind)

    override fun initView() = with(binding) {
        applyTopInset(accountScroll)
        tvRatingPrompt.text = getString(
            R.string.account_rating_prompt,
            requireContext().applicationInfo.loadLabel(requireContext().packageManager),
        )
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
        tvHelpCenter.singleClick {
            context?.start<HelpCenterActivity>()
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

    override fun initObserve() = Unit
}
