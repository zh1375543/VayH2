package com.novexa.platform.feature.accounts.presentation

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.novexa.platform.R
import com.novexa.platform.feature.accounts.presentation.adapter.PayoutAccountAdapter
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityLinkedAccountListBinding
import com.novexa.platform.feature.accounts.model.LinkedAccountResponse
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.ui.dialog.showConfirmDialog
import com.novexa.platform.core.common.util.PageLoadState
import com.novexa.platform.core.common.util.WALLET_INFO_PAGE
import com.novexa.platform.core.common.util.start
import com.novexa.platform.core.common.util.trackEvent
import com.novexa.platform.core.common.util.viewBinding

class LinkedAccountListActivity :
    BaseActivity<ActivityLinkedAccountListBinding>() {

    override val binding by viewBinding(ActivityLinkedAccountListBinding::inflate)
    private val vm by viewModels<LinkedAccountViewModel>()

    private val bankAdapter by lazy {
        PayoutAccountAdapter().apply {
            setOnChildClickListener { view, account, _ ->
                when (view.id) {
                    R.id.tvDefault -> showConfirmDialog(
                        getString(R.string.set_default_title),
                        getString(R.string.set_default_desc),
                        getString(R.string.closed),
                        getString(R.string.sure),
                        okAction = {
                            val updateDefaultState = {
                                items.filter { it.payWay == account.payWay }.forEach {
                                    it.isDefault = 0
                                }
                                account.isDefault = 1
                                notifyItemRangeChanged(0, itemCount, 0)
                            }
                            if (account.payWay == "WALLET") {
                                vm.setDefaultWallet(account.id?.toInt(), updateDefaultState)
                            } else {
                                vm.setDefaultCard(account.id.toString(), updateDefaultState)
                            }
                        },
                        cancelButtonSurfaceSecondary=true,
                        cancelAction = {}
                    )

                    R.id.tvDelete -> showConfirmDialog(
                        getString(R.string.unbind),
                        getString(R.string.unbind_desc),
                        getString(R.string.closed),
                        getString(R.string.sure),
                        okAction = {
                            vm.unBindCard(
                                account.id.toString(),
                                account.payWay ?: "CARD",
                            ) {
                                val currentPosition = items.indexOfFirst {
                                    it === account ||
                                        (it.id != null &&
                                            it.id == account.id &&
                                            it.payWay == account.payWay)
                                }
                                if (currentPosition != -1) {
                                    removeItem(currentPosition)
                                }
                            }
                        },
                        cancelAction = {}
                    )
                }
            }
        }
    }

    override fun initView() {
        trackPageEntry()
        bindAccountList()
    }

    /** Reports the page-entry tracking event for the wallet info screen. */
    private fun trackPageEntry() {
        trackEvent(WALLET_INFO_PAGE)
    }

    /** Binds the account adapter, the add-account entry, and the retry action. */
    private fun bindAccountList() = with(binding) {
        rvAccounts.adapter = bankAdapter
        addLayout.singleClick {
            start<LinkedAccountSetupActivity>()
        }
        pageState.setOnRetryClickListener {
            vm.getAccountList()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.getAccountList()
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        accountListState.observe(this@LinkedAccountListActivity) { state ->
            render(state)
        }
    }

    private fun render(state: PageLoadState<List<LinkedAccountResponse>>) = with(binding) {
        contentScroll.isVisible = state is PageLoadState.Content
        when (state) {
            PageLoadState.Loading -> pageState.showLoading()
            PageLoadState.Error -> pageState.showError()
            PageLoadState.Empty -> {
                if (bankAdapter.items.isNotEmpty()) {
                    bankAdapter.submitItems(emptyList())
                }
                pageState.showEmpty(R.mipmap.icon_banklist_empy, R.string.empty_bankcard)
            }
            is PageLoadState.Content -> {
                if (bankAdapter.items != state.data) {
                    bankAdapter.submitItems(state.data)
                }
                rvAccounts.isVisible = true
                pageState.hide()
            }
        }
    }
}
