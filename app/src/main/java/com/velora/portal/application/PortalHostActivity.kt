package com.velora.portal.application

import android.content.Context
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.common.data.PageHome
import com.velora.portal.platform.common.data.bean.TrackBean
import com.velora.portal.databinding.ScreenPortalHostBinding
import com.velora.portal.platform.common.data.ACT_clickActivity
import com.velora.portal.platform.common.data.ACT_clickMy
import com.velora.portal.platform.common.data.ACT_clickOrder
import com.velora.portal.platform.common.data.ACT_exit
import com.velora.portal.platform.common.data.ACT_in
import com.velora.portal.platform.common.data.PageExit
import com.velora.portal.platform.common.data.authConfigList
import com.velora.portal.journey.communication.inbox.presentation.MessageCenterActivity
import com.velora.portal.journey.lending.catalog.presentation.LoanDashboardViewModel
import com.velora.portal.journey.access.presentation.AuthStatusViewModel
import com.velora.portal.journey.lending.dashboard.presentation.VisitorPortalViewModel
import com.velora.portal.journey.lending.dashboard.presentation.HomeFragment
import com.velora.portal.journey.account.profile.presentation.ProfileCenterFragment
import com.velora.portal.journey.servicing.records.presentation.RecordCenterFragment
import com.velora.portal.platform.common.util.context.requireLogin
import com.velora.portal.platform.design.extension.addStatusBarTopMargin
import com.velora.portal.platform.common.util.showToastMessage
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.journey.lending.dashboard.presentation.dialog.showContactUsDialog
import com.velora.portal.platform.common.util.start

import com.velora.portal.platform.common.util.viewBinding

class PortalHostActivity : BaseActivity<ScreenPortalHostBinding>() {

    override val binding by viewBinding(ScreenPortalHostBinding::inflate)

    companion object {
        fun launch(
            context: Context,
            page: Int = 0,
            isFromAuth: Boolean = false,
            flags: Int = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        ) {
            context.start<PortalHostActivity> {
                putExtra("page", page)
                putExtra("isFromAuth", isFromAuth)
                addFlags(flags)
            }
        }
    }

    private val vm by viewModels<LoanDashboardViewModel>()
    private val authStatusVm by viewModels<AuthStatusViewModel>()
    private val guestDashboardVm by viewModels<VisitorPortalViewModel>()

    private var currentPage: Int = 0

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentPage = 0
        selectPage(currentPage)
        val isAuthToFillBank = intent.getBooleanExtra("isFromAuth", false)
        if (isAuthToFillBank) {
            vm.getAuthData()
        }
        recordDeviceSnapshot()
    }

    private var lastBackPressTime = 0L
    private val EXIT_INTERVAL = 2000

    override fun initView() {
        setupPager()
        setupClickListeners()
        setupBackPressHandler()
        recordDeviceSnapshot()
    }

    private fun setupPager() = with(binding) {
        vm.submitTrackingEvent(
            TrackBean(
                p = PageHome, act = ACT_in
            )
        )
        currentPage = intent.getIntExtra("page", 0)
        ivCustomer.addStatusBarTopMargin()
        selectPage(currentPage)
        vpMain.apply {
            offscreenPageLimit = 3
            isUserInputEnabled = false
            adapter = object : FragmentStateAdapter(this@PortalHostActivity) {
                override fun getItemCount(): Int = 3

                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> HomeFragment()
                        1 -> RecordCenterFragment()
                        else -> ProfileCenterFragment()
                    }
                }
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.apply {
                        tvHome.setSelected(position == 0)
                        tvOrder.setSelected(position == 1)
                        tvMine.setSelected(position == 2)
                        // Keep message and customer service available on every tab.  Only the
                        // brand area is hidden for the profile page, where it would overlap the
                        // profile header.
                        topGroup.isVisible = position != 2
                    }
                }
            })
        }
    }

    private fun updateStatusBarAppearance() {
        setStatusBarAppearance(
            statusBarColor = R.color.brand_primary,
            useDarkStatusBarIcons = false,
        )
        binding.topHeaderBackground.setBackgroundColor(
            getColor(R.color.brand_primary)
        )
        binding.tvAppName.setTextColor(
            getColor(R.color.text_inverse)
        )
    }

    private fun setupClickListeners() = with(binding) {
        vHome.singleClick {
            selectPage(0)
        }
        vOrder.singleClick {
            requireLogin {
                selectPage(1)
            }
        }
        vMine.singleClick {
            requireLogin {
                selectPage(2)
            }
        }
        ivMsg.singleClick {
            requireLogin {
                start<MessageCenterActivity>()
            }
        }
        ivCustomer.singleClick {
            guestDashboardVm.getUnAuthData(true)
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < EXIT_INTERVAL) {
                        isEnabled = false
                        vm.submitTrackingEvent(
                            TrackBean(
                                act = ACT_exit, result = PageHome, p = PageExit
                            )
                        )
                        onBackPressedDispatcher.onBackPressed() // normal back
                    } else {
                        lastBackPressTime = currentTime
                        getString(R.string.again_exit).showToastMessage()
                    }
                }
            })
    }

    private fun recordDeviceSnapshot() {
        MainApplication.appViewModel.postRiskInfo(PageHome) {}

    }

    fun selectPage(page: Int) {
        currentPage = page
        updateStatusBarAppearance()
        binding.apply {
            vpMain.setCurrentItem(page, false)
        }
        vm.submitTrackingEvent(
            TrackBean(
                p = PageHome, act = when (page) {
                    0 -> ACT_clickActivity
                    1 -> ACT_clickOrder
                    else -> ACT_clickMy
                }
            )
        )
    }

    override fun initObserve() = with(guestDashboardVm) {
        super.initObserve()
        result.observe(this@PortalHostActivity) {
            it?.let(::showContactUsDialog)
        }
    }

    override fun onResume() {
        super.onResume()
        authStatusVm.fetchAuthConfigList {
            authConfigList = it
        }
    }
}
