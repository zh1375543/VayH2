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
import com.velora.portal.databinding.ActivityGoalSettingGuideBinding

class GoalSettingGuideActivity : BaseActivity<ActivityGoalSettingGuideBinding>() {

    override val binding by viewBinding(ActivityGoalSettingGuideBinding::inflate)

    override fun initView() = with(binding) {
        applyTopInset(detailHeader)
        titleBar.setNavigationAction(::finish)
        quickTools.bindCalculationQuickTools()
        setSectionTitle(tvDefineTitle, R.string.calculator_goal_tip_define_title, R.string.calculator_goal_tip_define_highlight)
        setSectionTitle(tvTargetTitle, R.string.calculator_goal_tip_target_title, R.string.calculator_goal_tip_target_highlight)
        setSectionTitle(tvPlanTitle, R.string.calculator_goal_tip_plan_title, R.string.calculator_goal_tip_plan_highlight)
        setSectionTitle(tvProgressTitle, R.string.calculator_goal_tip_progress_title, R.string.calculator_goal_tip_progress_highlight)
    }

    private fun setSectionTitle(view: TextView, @StringRes titleRes: Int, @StringRes highlightRes: Int) {
        val title = getString(titleRes)
        val highlight = getString(highlightRes)
        val marker = "■ "
        val text = SpannableString(marker + title)
        val highlightStart = marker.length + title.indexOf(highlight)
        val highlightEnd = highlightStart + highlight.length
        val accentColor = ContextCompat.getColor(this, R.color.color_goal)
        text.setSpan(ForegroundColorSpan(accentColor), 0, marker.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (highlightStart >= marker.length) {
            text.setSpan(ForegroundColorSpan(accentColor), highlightStart, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(RelativeSizeSpan(HIGHLIGHT_TEXT_SCALE), highlightStart, highlightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        view.text = text
    }

    companion object {
        private const val HIGHLIGHT_TEXT_SCALE = 14f / 12f

        fun launch(context: Context) {
            context.startActivity(Intent(context, GoalSettingGuideActivity::class.java))
        }
    }
}
