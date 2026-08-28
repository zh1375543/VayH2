package com.velora.portal.moneyflow

import android.content.Context
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.velora.portal.R
import com.velora.portal.moneyflow.fg.MemberSpaceFragment
import com.velora.portal.moneyflow.fg.PayToolsFragment
import com.velora.portal.moneyflow.fg.IncomeSnapshotFragment
import com.velora.portal.moneyflow.fg.MoneyGuideFragment
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.platform.session.SessionStore
import com.velora.portal.databinding.ActivityPayPilotBinding
import com.velora.portal.journey.access.presentation.login.PhoneAuthActivity
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.common.util.showToastMessage

class PayPilotActivity : BaseActivity<ActivityPayPilotBinding>() {

    override val binding by viewBinding(ActivityPayPilotBinding::inflate)
    private var lastBackPressTime = 0L
    private var selectedPage = HOME_PAGE

    override fun initView() {
        initializePageContainer()
        bindPageNavigation(intent.getIntExtra(EXTRA_PAGE, HOME_PAGE))
        initializePageCallbacks()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        selectPage(intent.getIntExtra(EXTRA_PAGE, HOME_PAGE))
    }

    private fun initializePageContainer() {
        binding.vpMain.apply {
            isUserInputEnabled = false
            offscreenPageLimit = PAGE_COUNT - 1
            adapter = object : FragmentStateAdapter(this@PayPilotActivity) {
                override fun getItemCount(): Int = PAGE_COUNT

                override fun createFragment(position: Int): Fragment = when (position) {
                    HOME_PAGE -> IncomeSnapshotFragment()
                    CALCULATOR_PAGE -> PayToolsFragment()
                    TIPS_PAGE -> MoneyGuideFragment()
                    else -> MemberSpaceFragment()
                }
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    renderSelectedPage(position)
                }
            })
        }
    }

    private fun bindPageNavigation(initialPage: Int) = with(binding) {
        vHome.setOnClickListener { selectPage(HOME_PAGE) }
        vOrder.setOnClickListener { openSecuredPage(CALCULATOR_PAGE) }
        vStats.setOnClickListener { openSecuredPage(TIPS_PAGE) }
        vMine.setOnClickListener { openSecuredPage(ACCOUNT_PAGE) }

        selectPage(initialPage)
    }

    private fun initializePageCallbacks() {
        configurePageInsets()
        setupExitOnBackPress()
    }

    fun selectPage(page: Int) {
        val targetPage = page.coerceIn(HOME_PAGE, ACCOUNT_PAGE)
        renderSelectedPage(targetPage)
        binding.vpMain.setCurrentItem(targetPage, false)
    }

    private fun openSecuredPage(page: Int) {
        if (SessionStore.isLoggedIn) {
            selectPage(page)
        } else {
            PhoneAuthActivity.launchForPortal(this)
        }
    }

    private fun renderSelectedPage(page: Int) = with(binding) {
        selectedPage = page
        tvHome.isSelected = page == HOME_PAGE
        tvOrder.isSelected = page == CALCULATOR_PAGE
        tvStats.isSelected = page == TIPS_PAGE
        tvMine.isSelected = page == ACCOUNT_PAGE
        applyPageSystemBars()
        applyPageTopInset()

    }

    private fun applyPageSystemBars() {
        val isHomePage = selectedPage == HOME_PAGE
        setStatusBarAppearance(
            statusBarColor = if (isHomePage) R.color.brand_primary else R.color.transparent,
            useDarkStatusBarIcons = !isHomePage,
        )
    }

    private fun configurePageInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            applyPageTopInset(insets)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun applyPageTopInset(insets: WindowInsetsCompat? = null) {
        val statusBarTop = if (selectedPage == ACCOUNT_PAGE) {
            0
        } else {
            (insets ?: ViewCompat.getRootWindowInsets(binding.root))
                ?.getInsets(WindowInsetsCompat.Type.statusBars())
                ?.top
                ?: 0
        }
        binding.root.setPaddingRelative(0, statusBarTop, 0, 0)
    }

    private fun setupExitOnBackPress() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < EXIT_INTERVAL) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    } else {
                        lastBackPressTime = currentTime
                        getString(R.string.again_exit).showToastMessage()
                    }
                }
            },
        )
    }

    companion object {
        private const val EXTRA_PAGE = "page"
        private const val HOME_PAGE = 0
        private const val CALCULATOR_PAGE = 1
        private const val TIPS_PAGE = 2
        private const val ACCOUNT_PAGE = 3
        private const val PAGE_COUNT = 4
        private const val EXIT_INTERVAL = 2_000L

        fun launch(context: Context, page: Int = HOME_PAGE) {
            context.startActivity(
                Intent(context, PayPilotActivity::class.java)
                    .putExtra(EXTRA_PAGE, page)
            )
        }
    }
}
