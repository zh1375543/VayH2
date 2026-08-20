package com.novexa.platform.app

import android.content.Context
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.core.common.data.PageHome
import com.novexa.platform.core.common.data.bean.TrackBean
import com.novexa.platform.databinding.ActivityDashboardHostBinding
import com.novexa.platform.core.common.data.ACT_clickActivity
import com.novexa.platform.core.common.data.ACT_clickMy
import com.novexa.platform.core.common.data.ACT_clickOrder
import com.novexa.platform.core.common.data.ACT_exit
import com.novexa.platform.core.common.data.ACT_in
import com.novexa.platform.core.common.data.PageExit
import com.novexa.platform.core.common.data.authConfigList
import com.novexa.platform.feature.inbox.presentation.InboxActivity
import com.novexa.platform.feature.catalog.presentation.LoanDashboardViewModel
import com.novexa.platform.feature.onboarding.presentation.AuthStatusViewModel
import com.novexa.platform.feature.dashboard.presentation.VisitorPortalViewModel
import com.novexa.platform.feature.dashboard.presentation.HomeFragment
import com.novexa.platform.feature.profile.presentation.ProfileCenterFragment
import com.novexa.platform.feature.records.presentation.RecordCenterFragment
import com.novexa.platform.core.common.util.platform.requireLogin
import com.novexa.platform.core.ui.extension.addStatusBarTopMargin
import com.novexa.platform.core.common.util.showToastMessage
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.feature.dashboard.presentation.dialog.showContactUsDialog
import com.novexa.platform.core.common.util.start

import com.novexa.platform.core.common.util.viewBinding

class MainActivity : BaseActivity<ActivityDashboardHostBinding>() {

    override val binding by viewBinding(ActivityDashboardHostBinding::inflate)

    companion object {
        fun launch(
            context: Context,
            page: Int = 0,
            isFromAuth: Boolean = false,
            flags: Int = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        ) {
            context.start<MainActivity> {
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
        postDeviceInfo()
    }

    private var lastBackPressTime = 0L
    private val EXIT_INTERVAL = 2000

    override fun initView() {
        setupPager()
        setupClickListeners()
        setupBackPressHandler()
        postDeviceInfo()
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
            adapter = object : FragmentStateAdapter(this@MainActivity) {
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
                        topGroup.isVisible = position != 2
                    }
                }
            })
        }
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
                start<InboxActivity>()
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

    private fun postDeviceInfo() {
        MainApplication.appViewModel.postRiskInfo(PageHome) {}

    }

    fun selectPage(page: Int) {
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
        result.observe(this@MainActivity) {
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
