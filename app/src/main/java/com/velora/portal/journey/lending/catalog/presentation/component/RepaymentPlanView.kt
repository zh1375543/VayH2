package com.velora.portal.journey.lending.catalog.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.velora.portal.platform.common.util.LogUtil
import com.velora.portal.databinding.RepaymentPlanViewBinding
import com.velora.portal.domain.credit.model.CatalogItemBean
import com.velora.portal.journey.lending.catalog.presentation.adapter.InstallmentScheduleAdapter
import com.velora.portal.journey.lending.catalog.presentation.adapter.RepaymentPlanOptionAdapter

/** Lets a user select a repayment term and displays its installment schedule. */
class RepaymentPlanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    private val binding =
        RepaymentPlanViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val repaymentMenuAdapter by lazy { RepaymentPlanOptionAdapter() }
    private val installAdapter by lazy { InstallmentScheduleAdapter() }

    private var currentProduct: CatalogItemBean? = null
    private var hasInstallmentDetails = false

    var onTermChanged: ((productId: Long?, termId: Long?) -> Unit)? = null
    var onInstallmentChanged: ((productId: Long?, planNum: Int?) -> Unit)? = null
    var onPlanSelected: ((CatalogItemBean) -> Unit)? = null

    init {
        binding.rvTermOptions.adapter = repaymentMenuAdapter
        binding.rvInstallmentSchedule.adapter = installAdapter

        binding.ivExpandPlans.rotation = 0f
        val togglePlans = {
            val isPlanVisible = !binding.rvTermOptions.isVisible
            binding.rvTermOptions.isVisible = isPlanVisible
            binding.scheduleHeaderGroup.isVisible = isPlanVisible && hasInstallmentDetails
            binding.scheduleGroup.isVisible = isPlanVisible && hasInstallmentDetails
            binding.ivExpandPlans.rotation = if (isPlanVisible) 0f else 180f
        }
        binding.ivExpandPlans.setOnClickListener { togglePlans() }
        binding.tvPlanSectionTitle.setOnClickListener { togglePlans() }

        binding.ivExpandSchedule.rotation = 0f
        val toggleRepayment = {
            binding.scheduleGroup.isVisible = !binding.scheduleGroup.isVisible
            binding.ivExpandSchedule.rotation = if (binding.scheduleGroup.isVisible) 0f else 180f
        }
        binding.ivExpandSchedule.setOnClickListener { toggleRepayment() }
        binding.tvScheduleSectionTitle.setOnClickListener { toggleRepayment() }

        repaymentMenuAdapter.setOnItemClickListener { item, position ->
            if (position == repaymentMenuAdapter.selectPosition) return@setOnItemClickListener
            repaymentMenuAdapter.selectPosition = position
            currentProduct?.selectedTermIndex = position
            val productId = currentProduct?.id ?: currentProduct?.productId
            onTermChanged?.invoke(productId, item.id)
            updateUIByPlan(item)
            repaymentMenuAdapter.notifyItemRangeChanged(0, repaymentMenuAdapter.itemCount, 0)
        }
    }

    fun setData(product: CatalogItemBean) {
        currentProduct = product

        val hasPlans = !product.loanTermConfigDTOList.isNullOrEmpty()
        isVisible = hasPlans
        binding.termOptionsGroup.isVisible = hasPlans
        binding.rvTermOptions.isVisible = hasPlans

        if (hasPlans) {
            handlePlan(product)
        } else {
            repaymentMenuAdapter.selectPosition = -1
            repaymentMenuAdapter.submitItems(null)
            binding.scheduleGroup.isVisible = false
            binding.scheduleHeaderGroup.isVisible = false
            binding.ivExpandPlans.isVisible = false
            binding.tvPlanSectionTitle.isVisible = false
        }
    }

    private fun handlePlan(product: CatalogItemBean) {
        val list = product.loanTermConfigDTOList ?: return
        LogUtil.e("singleSelectIndex:${product.selectedTermIndex}")

        val isFirst = product.selectedTermIndex == null
        val index = if (
            product.selectedTermIndex != null &&
            product.selectedTermIndex!! >= 0 &&
            product.selectedTermIndex!! < list.size
        ) {
            product.selectedTermIndex!!
        } else {
            val defaultSignIndex = list.indexOfFirst { it.defaultSign == 1 }
            val isDefaultIndex = list.indexOfFirst { it.isDefault == 1 }
            when {
                defaultSignIndex >= 0 -> defaultSignIndex
                isDefaultIndex >= 0 -> isDefaultIndex
                else -> 0
            }.also { product.selectedTermIndex = it }
        }

        repaymentMenuAdapter.selectPosition = index
        repaymentMenuAdapter.submitItems(list)
        repaymentMenuAdapter.notifyDataSetChanged()
        binding.rvTermOptions.post { binding.rvTermOptions.scrollToPosition(index) }

        if (isFirst) {
            onTermChanged?.invoke(product.id ?: product.productId, list[index].id)
        }

        updateUIByPlan(list[index])
    }

    private fun updateUIByPlan(item: CatalogItemBean) {
        onPlanSelected?.invoke(item)
        updateInstallment(item)
    }

    private fun updateInstallment(item: CatalogItemBean) = with(binding) {
        val productId = currentProduct?.id ?: currentProduct?.productId
        tvPlanSectionTitle.isVisible = true
        ivExpandPlans.isVisible = true
        if (!item.productInstallmentPlanDTOList.isNullOrEmpty()) {
            hasInstallmentDetails = true
            val list = item.productInstallmentPlanDTOList
            val defaultSignIndex = list.indexOfFirst { it.defaultSign == 1 }
            val isDefaultIndex = list.indexOfFirst { it.isDefault == 1 }
            val index = when {
                defaultSignIndex >= 0 -> defaultSignIndex
                isDefaultIndex >= 0 -> isDefaultIndex
                else -> 0
            }.coerceIn(0, list.lastIndex)
            val plan = list[index]

            installAdapter.submitItems(plan.appRepaymentPlanDTOList)
            onInstallmentChanged?.invoke(productId, plan.planNums)

            scheduleGroup.isVisible = true
            scheduleHeaderGroup.isVisible = true
        } else {
            hasInstallmentDetails = false
            onInstallmentChanged?.invoke(productId, null)
            scheduleGroup.isVisible = false
            scheduleHeaderGroup.isVisible = false
        }
    }
}
