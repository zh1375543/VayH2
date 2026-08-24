package com.velora.portal.moneyflow.ac

import android.content.Context
import android.content.Intent
import com.velora.portal.moneyflow.bindCalculationQuickTools
import com.velora.portal.platform.common.util.viewBinding
import com.velora.portal.platform.design.base.BaseActivity
import com.velora.portal.databinding.ActivityGoalSettingGuideBinding

class GoalSettingGuideActivity : BaseActivity<ActivityGoalSettingGuideBinding>() {

    override val binding by viewBinding(ActivityGoalSettingGuideBinding::inflate)

    override fun initView() = with(binding) {
        setLightSystemBarIcons(enabled = true)
        titleBar.setNavigationAction(::finish)
        quickTools.bindCalculationQuickTools()
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, GoalSettingGuideActivity::class.java))
        }
    }
}
