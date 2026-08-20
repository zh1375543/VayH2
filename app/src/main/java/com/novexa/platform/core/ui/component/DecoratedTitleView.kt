package com.novexa.platform.core.ui.component

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.novexa.platform.R

/** A centered title framed by matching decorative icons. */
class DecoratedTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val startIcon = AppCompatImageView(context)
    private val titleView = AppCompatTextView(context)
    private val endIcon = AppCompatImageView(context)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        val defaultIconSize = context.resources.getDimensionPixelSize(R.dimen.dp_16)
        val defaultIconGap = context.resources.getDimensionPixelSize(R.dimen.dp_8)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DecoratedTitleView)
        val iconSize = typedArray.getDimensionPixelSize(
            R.styleable.DecoratedTitleView_decoratedTitleIconSize,
            defaultIconSize,
        )
        val iconGap = typedArray.getDimensionPixelSize(
            R.styleable.DecoratedTitleView_decoratedTitleIconGap,
            defaultIconGap,
        )
        val iconRes = typedArray.getResourceId(
            R.styleable.DecoratedTitleView_decoratedTitleIcon,
            R.mipmap.ic_state_img,
        )

        titleView.apply {
            text = typedArray.getString(R.styleable.DecoratedTitleView_decoratedTitleText)
            setTextColor(
                typedArray.getColor(
                    R.styleable.DecoratedTitleView_decoratedTitleTextColor,
                    ContextCompat.getColor(context, R.color.text_primary),
                ),
            )
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                typedArray.getDimension(
                    R.styleable.DecoratedTitleView_decoratedTitleTextSize,
                    context.resources.getDimension(R.dimen.sp_16),
                ),
            )
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        typedArray.recycle()

        addView(startIcon, iconLayoutParams(iconSize, 0, iconGap))
        addView(titleView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(endIcon, iconLayoutParams(iconSize, iconGap, 0))
        setIcon(iconRes)
    }

    fun setTitle(title: CharSequence?) {
        titleView.text = title
    }

    fun setTitleColor(@ColorInt color: Int) {
        titleView.setTextColor(color)
    }

    fun setIcon(@DrawableRes iconRes: Int) {
        startIcon.setImageResource(iconRes)
        endIcon.setImageResource(iconRes)
    }

    private fun iconLayoutParams(size: Int, startMargin: Int, endMargin: Int) =
        LayoutParams(size, size).apply {
            marginStart = startMargin
            marginEnd = endMargin
        }
}
