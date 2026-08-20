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
import com.novexa.platform.databinding.ActivityExpenseTrackingTipDetailBinding

class ExpenseTrackingTipDetailActivity : BaseActivity<ActivityExpenseTrackingTipDetailBinding>() {

    override val binding by viewBinding(ActivityExpenseTrackingTipDetailBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(detailHeader)
        titleBar.setNavigationAction(::finish)
        quickTools.bindCalculationQuickTools()
        setSectionTitle(tvHabitsTitle, R.string.calculator_track_tip_habits_title, R.string.calculator_track_tip_habits_highlight)
        setSectionTitle(tvCategorizeTitle, R.string.calculator_track_tip_categorize_title, R.string.calculator_track_tip_categorize_highlight)
        setSectionTitle(tvImproveTitle, R.string.calculator_track_tip_improve_title, R.string.calculator_track_tip_improve_highlight)
        setSectionTitle(tvReviewTitle, R.string.calculator_track_tip_review_title, R.string.calculator_track_tip_review_highlight)
        setExamples(tvExamples, R.string.calculator_track_tip_examples)
    }

    private fun setSectionTitle(view: TextView, @StringRes titleRes: Int, @StringRes highlightRes: Int) {
        val title = getString(titleRes)
        val highlight = getString(highlightRes)
        val marker = "■ "
        val text = SpannableString(marker + title)
        val highlightStart = marker.length + title.indexOf(highlight)
        val highlightEnd = highlightStart + highlight.length
        val accentColor = ContextCompat.getColor(this, R.color.color_track)
        text.setSpan(ForegroundColorSpan(accentColor), 0, marker.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (highlightStart >= marker.length) {
            text.setSpan(ForegroundColorSpan(accentColor), highlightStart, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(RelativeSizeSpan(HIGHLIGHT_TEXT_SCALE), highlightStart, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        view.text = text
    }

    private fun setExamples(view: TextView, @StringRes examplesRes: Int) {
        val accentColor = ContextCompat.getColor(this, R.color.color_track)
        val contentColor = ContextCompat.getColor(this, R.color.text_secondary)
        view.text = SpannableStringBuilder().apply {
            getString(examplesRes).lineSequence().forEachIndexed { index, example ->
                if (index > 0) append('\n')
                val bulletStart = length
                append("• ")
                setSpan(ForegroundColorSpan(accentColor), bulletStart, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val textStart = length
                append(example)
                setSpan(ForegroundColorSpan(contentColor), textStart, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    companion object {
        private const val HIGHLIGHT_TEXT_SCALE = 14f / 12f

        fun launch(context: Context) {
            context.startActivity(Intent(context, ExpenseTrackingTipDetailActivity::class.java))
        }
    }
}
