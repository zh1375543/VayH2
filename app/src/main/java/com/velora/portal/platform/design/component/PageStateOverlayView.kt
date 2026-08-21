package com.velora.portal.platform.design.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.velora.portal.databinding.ViewPageStateOverlayBinding
import com.velora.portal.platform.design.extension.singleClick

class PageStateOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = ViewPageStateOverlayBinding.inflate(
        LayoutInflater.from(context),
        this,
        true,
    )

    init {
        isVisible = false
    }

    fun showLoading() {
        show(OverlayState.LOADING)
    }

    fun showError() {
        show(OverlayState.ERROR)
    }

    fun showEmpty() {
        show(OverlayState.EMPTY)
    }

    fun showEmpty(@DrawableRes imageRes: Int, @StringRes textRes: Int) {
        binding.emptyView.tvEmptyState.apply {
            setCompoundDrawablesWithIntrinsicBounds(0, imageRes, 0, 0)
            setText(textRes)
        }
        show(OverlayState.EMPTY)
    }

    fun hide() {
        isVisible = false
    }

    fun setOnRetryClickListener(listener: () -> Unit) {
        binding.errorState.tvErrorRetry.singleClick { listener() }
    }

    private fun show(state: OverlayState) {
        isVisible = true
        binding.loadingState.root.isVisible = state == OverlayState.LOADING
        binding.errorState.root.isVisible = state == OverlayState.ERROR
        binding.emptyView.root.isVisible = state == OverlayState.EMPTY
    }

    private enum class OverlayState {
        LOADING,
        ERROR,
        EMPTY,
    }
}
