package com.velora.portal.moneyflow.ac

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.velora.portal.R
import com.velora.portal.moneyflow.bindCalculationQuickTools
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivitySavingsPlaybookBinding

class SavingsPlaybookActivity : BaseActivity<ActivitySavingsPlaybookBinding>() {

    override val binding by viewBinding(ActivitySavingsPlaybookBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(detailHeader)
        titleBar.setNavigationAction(::finish)
        quickTools.bindCalculationQuickTools()
        setSectionTitle(
            tvStartPlanTitle,
            R.string.calculator_savings_tip_start_title,
            R.string.calculator_savings_tip_start_highlight,
        )
        setSectionTitle(
            tvSaveFirstTitle,
            R.string.calculator_savings_tip_save_first_title,
            R.string.calculator_savings_tip_save_first_highlight,
        )
        setSectionTitle(
            tvReduceExpensesTitle,
            R.string.calculator_savings_tip_reduce_title,
            R.string.calculator_savings_tip_reduce_highlight,
        )
        setSectionTitle(
            tvSavingsGoalTitle,
            R.string.calculator_savings_tip_goal_title,
            R.string.calculator_savings_tip_goal_highlight,
        )
    }

    private fun setSectionTitle(
        view: TextView,
        @StringRes titleRes: Int,
        @StringRes highlightRes: Int,
    ) {
        val title = getString(titleRes)
        val marker = "■ "
        val text = SpannableString(marker + title)
        val highlightStart = marker.length + title.indexOf(getString(highlightRes))
        val highlightEnd = highlightStart + getString(highlightRes).length
        val brandColor = ContextCompat.getColor(this, R.color.brand_primary)
        text.setSpan(ForegroundColorSpan(brandColor), 0, marker.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (highlightStart >= marker.length) {
            text.setSpan(
                ForegroundColorSpan(brandColor),
                highlightStart,
                highlightEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            text.setSpan(
                RelativeSizeSpan(HIGHLIGHT_TEXT_SCALE),
                highlightStart,
                highlightEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        view.text = text
    }

    companion object {
        private const val HIGHLIGHT_TEXT_SCALE = 13f / 11f

        fun launch(context: Context) {
            context.startActivity(Intent(context, SavingsPlaybookActivity::class.java))
        }
    }
}
