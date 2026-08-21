package com.velora.portal.journey.communication.inbox.presentation

import android.Manifest
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ScreenMessageCenterBinding
import com.velora.portal.journey.communication.inbox.model.InboxMessageRecord
import com.velora.portal.journey.communication.inbox.presentation.adapter.MessageInboxAdapter
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.design.dialog.showConfirmDialog
import com.velora.portal.platform.common.util.PageLoadState
import com.velora.portal.platform.common.util.PermissionCoordinator
import com.velora.portal.platform.common.util.start
import com.velora.portal.platform.common.util.viewBinding

class MessageCenterActivity : BaseActivity<ScreenMessageCenterBinding>() {

    override val binding by viewBinding(ScreenMessageCenterBinding::inflate)
    private val vm by viewModels<InboxCenterViewModel>()

    private val messageAdapter by lazy {
        MessageInboxAdapter().apply {
            setOnItemClickListener { item, _ ->
                start<MessageDetailActivity> {
                    putExtra("msg", item)
                }
                vm.markAsRead(item)
            }
        }
    }

    override fun initView() {
        setupMessageList()
        setupLoadingActions()
        setupToolbarActions()
    }

    private fun setupMessageList() = with(binding) {
        rvMessage.adapter = messageAdapter
    }

    private fun setupLoadingActions() = with(binding) {
        pageState.setOnRetryClickListener {
            vm.getMessageList()
        }
        vm.getMessageList()
    }

    private fun setupToolbarActions() = with(binding) {
        tvOpen.singleClick {
            PermissionCoordinator.request(
                this@MessageCenterActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                onDenied = { isPermanentlyDenied, deniedPermissions ->
                    if (isPermanentlyDenied) {
                        PermissionCoordinator.openSystemSettings(
                            this@MessageCenterActivity,
                            deniedPermissions,
                        )
                    }
                },
                showSettingsGuide = false,
            ) {}
        }
        titleBar.setAction {
            showConfirmDialog(title = getString(R.string.read_msg_title), desc = "") {
                vm.markAllAsRead()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.noticeLayout.isVisible = !PermissionCoordinator.hasPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    override fun initObserve() = with(vm) {
        super.initObserve()
        messageListState.observe(this@MessageCenterActivity) { state ->
            render(state)
        }
    }

    private fun render(state: PageLoadState<List<InboxMessageRecord>>) = with(binding) {
        rvMessage.isVisible = state is PageLoadState.Content
        titleBar.showAction(false)
        when (state) {
            PageLoadState.Loading -> pageState.showLoading()
            PageLoadState.Error -> pageState.showError()
            PageLoadState.Empty -> {
                if (messageAdapter.items.isNotEmpty()) {
                    messageAdapter.submitItems(emptyList())
                }
                pageState.showEmpty(R.mipmap.icon_message_empy, R.string.empty_message)
            }
            is PageLoadState.Content -> {
                if (messageAdapter.items != state.data) {
                    messageAdapter.submitItems(state.data)
                }
                titleBar.showAction(state.data.any { !it.readStatus })
                pageState.hide()
            }
        }
    }
}
