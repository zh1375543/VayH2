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
import com.velora.portal.databinding.ActivityExpenseHabitsGuideBinding

class ExpenseHabitsGuideActivity : BaseActivity<ActivityExpenseHabitsGuideBinding>() {

    override val binding by viewBinding(ActivityExpenseHabitsGuideBinding::inflate)

    override fun initView() = with(binding) {
        setLightSystemBarIcons(enabled = true)
        titleBar.setNavigationAction(::finish)
        quickTools.bindCalculationQuickTools()
    }


    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, ExpenseHabitsGuideActivity::class.java))
        }
    }
}
