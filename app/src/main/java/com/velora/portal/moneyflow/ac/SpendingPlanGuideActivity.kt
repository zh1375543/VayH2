package com.velora.portal.moneyflow.ac

import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.velora.portal.R
import com.velora.portal.moneyflow.bindCalculationQuickTools
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivitySpendingPlanGuideBinding

class SpendingPlanGuideActivity : BaseActivity<ActivitySpendingPlanGuideBinding>() {

    override val binding by viewBinding(ActivitySpendingPlanGuideBinding::inflate)

    override fun initView() {
        setupGuideChrome()
        populateGuideContent()
    }

    private fun setupGuideChrome() = with(binding) {
        setLightSystemBarIcons(enabled = true)
        titleBar.setNavigationAction(::finish)
    }

    private fun populateGuideContent() = with(binding) {
        quickTools.bindCalculationQuickTools()
        setExamples(tvNeedsExamples, R.string.calculator_budget_tip_needs_examples)
        setExamples(tvWantsExamples, R.string.calculator_budget_tip_wants_examples)
        setExamples(tvSavingsExamples, R.string.calculator_budget_tip_savings_examples)
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
        fun launch(context: Context) {
            context.startActivity(Intent(context, SpendingPlanGuideActivity::class.java))
        }
    }
}
