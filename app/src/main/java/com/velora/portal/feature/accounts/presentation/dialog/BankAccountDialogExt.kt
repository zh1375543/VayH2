package com.velora.portal.feature.accounts.presentation.dialog

import android.content.Context
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.velora.portal.R
import com.velora.portal.platform.design.base.BaseDialog
import com.velora.portal.platform.design.base.BaseSheetDialog
import com.velora.portal.feature.accounts.model.LinkedAccountResponse
import com.velora.portal.feature.accounts.model.AccountChannelResponse
import com.velora.portal.feature.accounts.model.AccountMethodResponse
import com.velora.portal.databinding.BankCardErrorDialogBinding
import com.velora.portal.databinding.ChooseAccountsDialogBinding
import com.velora.portal.databinding.ChooseBankDialogBinding
import com.velora.portal.databinding.ChooseWalletDialogBinding
import com.velora.portal.databinding.DialogDisbursementMethodBinding
import com.velora.portal.feature.accounts.presentation.LinkedAccountSetupActivity
import com.velora.portal.feature.accounts.presentation.adapter.PayoutAccountPickerAdapter
import com.velora.portal.feature.accounts.presentation.adapter.ChooseBankDialogAdapter
import com.velora.portal.feature.accounts.presentation.adapter.ChooseWalletDialogAdapter
import com.velora.portal.platform.design.extension.hideKeyboard
import com.velora.portal.platform.design.extension.singleClick
import com.velora.portal.platform.common.util.start
import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

fun Context.showBankCardErrorDialog(
    desc: String = getString(R.string.card_error_tips),
    cancel: String = getString(R.string.already_edited),
    ok: String = getString(R.string.revise),
    cancelAction: () -> Unit = {},
    okAction: () -> Unit,
) {
    object : BaseDialog<BankCardErrorDialogBinding>(
        this,
        BankCardErrorDialogBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            tvDesc.text = desc
            btnClose.text = cancel
            btnSure.text = ok
            btnClose.singleClick {
                dismiss()
                cancelAction()
            }
            btnSure.singleClick {
                dismiss()
                okAction()
            }
        }
    }.show()
}


fun Context.chooseAccountsDialog(
    cardNo: String?,
    list: List<LinkedAccountResponse>,
    isRepay: Boolean = false,
    title: String = getString(R.string.select_account),
    selectedAccountId: Long? = null,
    selectedPayWay: String? = null,
    applyAction: (info: LinkedAccountResponse) -> Unit,
) {
    object : BaseSheetDialog<ChooseAccountsDialogBinding>(this, ChooseAccountsDialogBinding::inflate) {
        override fun initView() = with(binding) {
            tvTitle.text = title
            tvAdd.isVisible = !isRepay
            val currentAccountIndex = selectedAccountId?.let { accountId ->
                list.indexOfFirst {
                    it.id == accountId && (selectedPayWay == null || it.payWay == selectedPayWay)
                }
            } ?: -1
            val currentCardIndex = list.indexOfFirst { it.bankNo == cardNo || it.account == cardNo }
            val defaultAccountIndex = list.indexOfFirst { it.isDefault == 1 }
            val index = max(
                0,
                when {
                    currentAccountIndex >= 0 -> currentAccountIndex
                    currentCardIndex >= 0 -> currentCardIndex
                    else -> defaultAccountIndex
                },
            )
            fun updateCardCount(selectedIndex: Int) {
                tvCardCount.text = getString(R.string.bank_card_selected_count, selectedIndex + 1, list.size)
            }
            updateCardCount(index)
            val adapter =
                PayoutAccountPickerAdapter(index).apply {
                    submitItems(list)
                    setOnItemClickListener { _, index ->
                        selectPosition = index
                        updateCardCount(index)
                        notifyItemRangeChanged(0, itemCount, 0)
                    }
            }
            rvCard.adapter = adapter
            rvCard.post {
                val maxListHeight = (resources.displayMetrics.heightPixels * 0.4f).toInt()
                if (rvCard.computeVerticalScrollRange() > maxListHeight) {
                    rvCard.layoutParams = rvCard.layoutParams.apply { height = maxListHeight }
                    rvCard.requestLayout()
                }
                rvCard.scrollToPosition(index)
            }
            tvAdd.singleClick {
                dismiss()
                start<LinkedAccountSetupActivity>()
            }
            BtnApply.singleClick {
                dismiss()
                applyAction.invoke(adapter.items[adapter.selectPosition])
            }
        }
    }.show()
}

fun Context.chooseBankDialog(list: List<AccountChannelResponse>, chooseAction: (AccountChannelResponse) -> Unit) {
    object : BaseSheetDialog<ChooseBankDialogBinding>(this, ChooseBankDialogBinding::inflate) {
        override fun initView() = with(binding) {
            super.initView()
            val adapter = ChooseBankDialogAdapter().apply {
                setOnItemClickListener { item, _ ->
                    chooseAction.invoke(item)
                    dismiss()
                }
            }
            adapter.submitItems(list)
            tvEmpty.isVisible = false
            rvBank.adapter = adapter
            etSearch.doAfterTextChanged { text ->
                val newList = filterBankChannels(list, text?.toString().orEmpty())
                adapter.submitItems(newList)
                tvEmpty.isVisible = newList.isEmpty()
                rvBank.isVisible = newList.isNotEmpty()
            }
            root.setOnClickListener {
                etSearch.hideKeyboard()
            }
            tvEmpty.setOnClickListener { etSearch.hideKeyboard() }
        }
    }.show()
}

fun Context.chooseWalletDialog(list: List<AccountMethodResponse>, chooseAction: (AccountMethodResponse) -> Unit) {
    object : BaseSheetDialog<ChooseWalletDialogBinding>(this, ChooseWalletDialogBinding::inflate) {
        override fun initView() = with(binding) {
            super.initView()
            val adapter = ChooseWalletDialogAdapter().apply {
                setOnItemClickListener { item, _ ->
                    chooseAction(item)
                    dismiss()
                }
            }
            adapter.submitItems(list)
            tvEmpty.isVisible = false
            rvWallet.adapter = adapter
            etSearch.doAfterTextChanged { text ->
                val filteredItems = filterWallets(list, text?.toString().orEmpty())
                adapter.submitItems(filteredItems)
                tvEmpty.isVisible = filteredItems.isEmpty()
                rvWallet.isVisible = filteredItems.isNotEmpty()
            }
            root.setOnClickListener { etSearch.hideKeyboard() }
            tvEmpty.setOnClickListener { etSearch.hideKeyboard() }
        }
    }.show()
}

fun Context.showWithdrawMethodDialog(
    walletAction: () -> Unit,
    bankAction: () -> Unit,
) {
    object : BaseDialog<DialogDisbursementMethodBinding>(
        this,
        DialogDisbursementMethodBinding::inflate,
    ) {
        override fun initView() = with(binding) {
            super.initView()
            cancelView.singleClick { dismiss() }
            eWalletOptionView.singleClick {
                dismiss()
                walletAction()
            }
            bankOptionView.singleClick {
                dismiss()
                bankAction()
            }
        }
    }.show()
}

private fun filterBankChannels(
    list: List<AccountChannelResponse>,
    keyword: String,
): List<AccountChannelResponse> {
    val query = keyword.normalizeSearchKeyword()
    if (query.isBlank()) return list

    return list.mapNotNull { item ->
        val longCode = item.longCode.normalizeSearchKeyword()
        val bankName = item.bankName.normalizeSearchKeyword()
        val rank = when {
            longCode == query || bankName == query -> 0
            longCode.startsWith(query) || bankName.startsWith(query) -> 1
            longCode.contains(query) || bankName.contains(query) -> 2
            else -> return@mapNotNull null
        }
        rank to item
    }.sortedBy { it.first }
        .map { it.second }
}

private fun filterWallets(
    list: List<AccountMethodResponse>,
    keyword: String,
): List<AccountMethodResponse> {
    val query = keyword.normalizeSearchKeyword()
    if (query.isBlank()) return list

    return list.mapNotNull { item ->
        val searchableFields = listOf(
            item.walletName,
            item.walletCode,
            item.walletType,
            item.walletDesc,
        ).map { it.normalizeSearchKeyword() }

        val rank = when {
            searchableFields.any { it == query } -> 0
            searchableFields.any { it.startsWith(query) } -> 1
            searchableFields.any { it.contains(query) } -> 2
            else -> return@mapNotNull null
        }
        rank to item
    }.sortedBy { it.first }
        .map { it.second }
}

private fun String?.normalizeSearchKeyword(): String {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return ""
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase(Locale.ROOT)
}
