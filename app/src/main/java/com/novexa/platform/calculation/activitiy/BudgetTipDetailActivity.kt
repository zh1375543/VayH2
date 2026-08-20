package com.novexa.platform.calculation.activitiy

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.novexa.platform.R
import com.novexa.platform.calculation.bindCalculationQuickTools
import com.novexa.platform.core.common.util.viewBinding
import com.novexa.platform.core.ui.base.BaseActivity
import com.novexa.platform.databinding.ActivityBudgetTipDetailBinding

class BudgetTipDetailActivity : BaseActivity<ActivityBudgetTipDetailBinding>() {

    override val binding by viewBinding(ActivityBudgetTipDetailBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(detailHeader)
        titleBar.setNavigationAction(::finish)
        quickTools.bindCalculationQuickTools()
        setSectionTitle(tvRuleTitle, R.string.calculator_budget_tip_rule_title, R.string.calculator_budget_tip_rule_highlight)
        setSectionTitle(tvNeedsTitle, R.string.calculator_budget_tip_needs_title, R.string.calculator_budget_tip_needs_highlight)
        setSectionTitle(tvWantsTitle, R.string.calculator_budget_tip_wants_title, R.string.calculator_budget_tip_wants_highlight)
        setSectionTitle(tvSavingsTitle, R.string.calculator_budget_tip_savings_title, R.string.calculator_budget_tip_savings_highlight)
        setSectionTitle(tvAdjustTitle, R.string.calculator_budget_tip_adjust_title, R.string.calculator_budget_tip_adjust_highlight)
        setExamples(tvNeedsExamples, R.string.calculator_budget_tip_needs_examples)
        setExamples(tvWantsExamples, R.string.calculator_budget_tip_wants_examples)
        setExamples(tvSavingsExamples, R.string.calculator_budget_tip_savings_examples)
    }

    private fun setSectionTitle(
        view: TextView,
        @StringRes titleRes: Int,
        @StringRes highlightRes: Int,
    ) {
        val title = getString(titleRes)
        val highlight = getString(highlightRes)
        val marker = "■ "
        val text = SpannableString(marker + title)
        val highlightStart = marker.length + title.indexOf(highlight)
        val highlightEnd = highlightStart + highlight.length
        val accentColor = ContextCompat.getColor(this, R.color.brand_secondary)
        text.setSpan(ForegroundColorSpan(accentColor), 0, marker.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (highlightStart >= marker.length) {
            text.setSpan(ForegroundColorSpan(accentColor), highlightStart, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(RelativeSizeSpan(HIGHLIGHT_TEXT_SCALE), highlightStart, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        view.text = text
    }

    private fun setExamples(view: TextView, @StringRes examplesRes: Int) {
        val accentColor = ContextCompat.getColor(this, R.color.brand_secondary)
        val contentColor = ContextCompat.getColor(this, R.color.text_secondary)
        view.text = SpannableStringBuilder().apply {
            getString(examplesRes).lineSequence().forEachIndexed { index, example ->
                if (index > 0) append('\n')
                val bulletStart = length
                append("• ")
                setSpan(ForegroundColorSpan(accentColor), bulletStart, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val exampleStart = length
                append(example)
                setSpan(ForegroundColorSpan(contentColor), exampleStart, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    companion object {
        private const val HIGHLIGHT_TEXT_SCALE = 14f / 12f

        fun launch(context: Context) {
            context.startActivity(Intent(context, BudgetTipDetailActivity::class.java))
        }
    }
}
