package com.velora.portal.journey.lending.catalog.presentation.adapter

import androidx.core.view.isVisible
import com.velora.portal.platform.design.base.BaseAdapter
import com.velora.portal.databinding.ItemComboBinding
import com.velora.portal.domain.credit.model.CatalogEntry
import com.velora.portal.platform.common.util.text.formatAmountWithPrefix

class ComboAdapter : BaseAdapter<CatalogEntry, ItemComboBinding>(ItemComboBinding::inflate) {

    override fun bindItem(
        binding: ItemComboBinding,
        item: CatalogEntry,
        position: Int,
    ) = with(binding) {
        offerSummaryCard.bind(
            item,
            if (item.canApply) {
                item.maxLoanAmount.formatAmountWithPrefix(item.currencySymbol)
            } else {
                item.loanAmountRange.orEmpty()
            },
        )
        offerDetailCard.isVisible = item.isExpand
        offerSummaryCard.setExpanded(item.isExpand)
        offerSummaryCard.setOnDetailsClickListener {
            item.isExpand = !item.isExpand
            notifyItemChanged(position)
        }
        offerDetailCard.apply {
            if (item.selectedTermIndex == null) {
                val defaultSignIndex =
                    item.loanTermConfigDTOList?.indexOfFirst { it1 -> it1.defaultSign == 1 }
                        ?: -1
                item.selectedTermIndex = when {
                    defaultSignIndex >= 0 -> defaultSignIndex
                    else -> 0
                }
            }
            onPlanSelected = { plan ->
                bindHeaderDetail(plan, plan.currencySymbol ?: item.currencySymbol)
            }
            bindHeaderDetail(item, item.currencySymbol)
            setData(item)
        }
        Unit
    }

    fun submitItemsWithState(newItems: List<CatalogEntry>?) {
        val oldItems = items  // previous list

        newItems?.forEach { newItem ->
            val newId = newItem.id ?: newItem.productId
            // find the matching old item and merge UI state
            val oldItem = oldItems.find {
                (it.id ?: it.productId) == newId
            }
            if (oldItem != null) {
                newItem.isExpand = oldItem.isExpand
                newItem.selectedTermIndex = oldItem.selectedTermIndex
                newItem.isPlanLayoutVisible = oldItem.isPlanLayoutVisible
            }
        }
        submitItems(newItems)
    }
}
