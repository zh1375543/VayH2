package com.velora.portal.core.ui.component

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.velora.portal.R
import com.velora.portal.databinding.ViewScreenHeaderBinding
import com.velora.portal.core.ui.extension.addStatusBarTopMargin
import com.velora.portal.core.ui.extension.singleClick

class PageHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding =
        ViewScreenHeaderBinding.inflate(LayoutInflater.from(context), this)

    private var fitStatusBar = true

    init {
        applyXmlAttributes(attrs)
        configureDefaultNavigation()
        applyStatusBarSpacingIfNeeded()
    }

    private fun applyXmlAttributes(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.PageHeaderView, 0, 0).apply {
            try {
                updateNavigationIconColor(ContextCompat.getColor(context, R.color.black))
                updateTitle(getString(R.styleable.PageHeaderView_centerText))
                setAction(getString(R.styleable.PageHeaderView_rightText))
                val rightImage = getDrawable(R.styleable.PageHeaderView_rightImage)
                setRightImage(rightImage)
                showRightImage(
                    getBoolean(R.styleable.PageHeaderView_showRightImage, rightImage != null),
                )
                if (hasValue(R.styleable.PageHeaderView_tintColor)) {
                    updateContentColor(getColor(R.styleable.PageHeaderView_tintColor, 0))
                }
                if (hasValue(R.styleable.PageHeaderView_navigationIconColor)) {
                    updateNavigationIconColor(
                        getColor(R.styleable.PageHeaderView_navigationIconColor, 0),
                    )
                }
                if (hasValue(R.styleable.PageHeaderView_navigationMarginStart)) {
                    val params = binding.ivBack.layoutParams as LayoutParams
                    params.marginStart = getDimensionPixelSize(
                        R.styleable.PageHeaderView_navigationMarginStart,
                        params.marginStart,
                    )
                    binding.ivBack.layoutParams = params
                }
                fitStatusBar = getBoolean(R.styleable.PageHeaderView_fitStatusBar, true)
            } finally {
                recycle()
            }
        }
        updateRightAction()
    }

    private fun configureDefaultNavigation() {
        setNavigationAction {
            (context as? Activity)?.finish()
        }
    }

    private fun applyStatusBarSpacingIfNeeded() {
        if (fitStatusBar) {
            post { addStatusBarTopMargin() }
        }
    }

    private fun updateRightAction() {
        binding.tvBarRight.isVisible = !binding.tvBarRight.text.isNullOrEmpty()
    }

    fun setNavigationAction(action: () -> Unit) {
        binding.ivBack.singleClick { action() }
    }

    fun setAction(text: CharSequence? = null, action: (() -> Unit)? = null) {
        text?.let { binding.tvBarRight.text = it }
        binding.tvBarRight.setOnClickListener(null)
        action?.let { clickAction ->
            binding.tvBarRight.singleClick { clickAction() }
        }
    }

    fun updateTitle(title: CharSequence?) {
        binding.tvBarTitle.text = title ?: ""
    }

    fun updateContentColor(@ColorInt color: Int) {
        updateNavigationIconColor(color)
        binding.tvBarTitle.setTextColor(color)
        binding.tvBarRight.setTextColor(color)
    }

    /** Updates only the back navigation icon color. Defaults to black. */
    fun updateNavigationIconColor(@ColorInt color: Int) {
        binding.ivBack.imageTintList = ColorStateList.valueOf(color)
    }

    fun showAction(visible: Boolean) {
        binding.tvBarRight.isVisible = visible
    }

    /** Sets the image displayed at the right side of the header. */
    fun setRightImage(image: Drawable?) {
        binding.ivRightCoin.setImageDrawable(image)
        binding.ivRightCoin.isVisible = image != null
    }

    /** Sets the image displayed at the right side of the header from a drawable resource. */
    fun setRightImage(@DrawableRes imageRes: Int) {
        binding.ivRightCoin.setImageResource(imageRes)
        binding.ivRightCoin.isVisible = true
    }

    fun setRightImageAction(action: (() -> Unit)?) {
        binding.ivRightCoin.setOnClickListener(null)
        action?.let { clickAction ->
            binding.ivRightCoin.singleClick { clickAction() }
        }
    }

    fun showRightImage(visible: Boolean) {
        binding.ivRightCoin.isVisible = visible
    }

    fun showNavigation(visible: Boolean) {
        binding.ivBack.isVisible = visible
    }
}
