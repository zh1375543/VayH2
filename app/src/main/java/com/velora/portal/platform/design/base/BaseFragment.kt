package com.velora.portal.platform.design.base

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

import androidx.annotation.LayoutRes

abstract class BaseFragment<VB : ViewBinding>(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    protected abstract val binding: VB

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
    }

    protected fun applyTopInset(target: View) {
        val startPadding = target.paddingStart
        val topPadding = target.paddingTop
        val endPadding = target.paddingEnd
        val bottomPadding = target.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPaddingRelative(
                startPadding,
                topPadding + systemBars.top,
                endPadding,
                bottomPadding,
            )
            insets
        }
        ViewCompat.requestApplyInsets(target)
    }

    abstract fun initView()

    abstract fun initObserve()
}
