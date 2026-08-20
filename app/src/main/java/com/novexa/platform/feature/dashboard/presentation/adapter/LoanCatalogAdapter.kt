package com.novexa.platform.feature.dashboard.presentation.adapter

import androidx.core.view.isVisible
import com.novexa.platform.R
import com.novexa.platform.core.ui.base.BaseAdapter
import com.novexa.platform.databinding.ItemHomeProductBinding
import com.novexa.platform.feature.dashboard.presentation.state.HomeProductUi
import com.novexa.platform.core.ui.extension.loadImage
import com.novexa.platform.core.common.util.text.formatAmountWithPrefix
import com.novexa.platform.core.common.util.platform.formatLoanTerm

class LoanCatalogAdapter :
    BaseAdapter<HomeProductUi, ItemHomeProductBinding>(ItemHomeProductBinding::inflate) {

    override fun bindItem(
        binding: ItemHomeProductBinding,
        item: HomeProductUi,
        position: Int,
    ) = with(binding) {
        val product = item.product
        ivIcon.loadImage(product.productImageUrl, R.mipmap.ic_product_defalut)
        tvName.text = product.productName
        tvOfferLimitLabel.text = context.getString(R.string.home_product_loan_amount_title)
        tvOfferLimit.text =
            if (item.canApply) product.maxLoanAmount.formatAmountWithPrefix(product.currencySymbol) else product.loanAmountRange
        tvOfferTerm.text = context.formatLoanTerm(product.timeLimit)
        btnOfferAction.isEnabled = item.canApply
        unavailableOverlay.isVisible = !btnOfferAction.isEnabled
        ivNewOfferBadge.setImageResource(R.mipmap.ic_new_product)
        ivNewOfferBadge.isVisible = product.newSign == 1 && !product.isTogether
        rvOfferBenefits.adapter = LoanFeatureTagAdapter().apply {
            submitItems(product.tagList?.distinct())
        }
        btnOfferAction.text =
            context.getString(if (product.showConditionTypeSign != "1") R.string.withdrawal else R.string.go_add_info_str)
    }

    override fun bindChildClickListeners(
        binding: ItemHomeProductBinding,
        item: HomeProductUi,
        position: Int,
    ) = with(binding) {
        super.bindChildClickListeners(binding, item, position)
        btnOfferAction.setOnClickListener {
            dispatchChildClick(it, item, position)
        }
    }
}
