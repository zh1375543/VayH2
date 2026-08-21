package com.velora.portal.platform.design.component

import android.R as AndroidR
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import com.velora.portal.R

/** Text label with XML-configurable background, text, and icon states. */
class StatefulLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val appearance = readAppearance(attrs)
    private val normalDrawables = compoundDrawablesRelative.copyOf()

    init {
        background = buildBackground(appearance)
        applyTextColors(appearance.text)
        applyStatefulDrawables(appearance.icons)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        background?.setBounds(0, 0, width, height)
        updateCompoundDrawableBounds()
    }

    private fun readAppearance(attrs: AttributeSet?): LabelAppearance {
        val normalTextColor = currentTextColor
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.StatefulLabelView)
        try {
            return attributes.let { a ->
            val radius = a.getDimensionPixelOffset(R.styleable.StatefulLabelView_labelCornerRadius, 0)
            val corners = LabelCorners(
                topLeft = a.dimensionOr(R.styleable.StatefulLabelView_labelTopLeftRadius, radius),
                topRight = a.dimensionOr(R.styleable.StatefulLabelView_labelTopRightRadius, radius),
                bottomRight = a.dimensionOr(R.styleable.StatefulLabelView_labelBottomRightRadius, radius),
                bottomLeft = a.dimensionOr(R.styleable.StatefulLabelView_labelBottomLeftRadius, radius),
            )
            val direction = a.getInt(R.styleable.StatefulLabelView_labelGradientDirection, 0)
            val normalGradient = a.gradient(
                R.styleable.StatefulLabelView_labelGradientStartColor,
                R.styleable.StatefulLabelView_labelGradientCenterColor,
                R.styleable.StatefulLabelView_labelGradientEndColor,
            )
            LabelAppearance(
                corners = corners,
                borderWidth = a.getDimensionPixelOffset(R.styleable.StatefulLabelView_labelBorderWidth, 0),
                direction = direction,
                derivePressed = a.getBoolean(R.styleable.StatefulLabelView_labelDerivePressedState, false),
                normal = LabelBackgroundState(
                    a.getColor(R.styleable.StatefulLabelView_labelFillColor, Color.TRANSPARENT),
                    a.getColor(R.styleable.StatefulLabelView_labelBorderColor, Color.TRANSPARENT), normalGradient,
                ),
                pressed = LabelBackgroundState(
                    a.getColor(R.styleable.StatefulLabelView_labelPressedFillColor, UNSET),
                    a.getColor(R.styleable.StatefulLabelView_labelPressedBorderColor, UNSET),
                    a.gradient(R.styleable.StatefulLabelView_labelPressedGradientStartColor, null, R.styleable.StatefulLabelView_labelPressedGradientEndColor),
                ),
                focused = LabelBackgroundState(
                    a.getColor(R.styleable.StatefulLabelView_labelFillColor, Color.TRANSPARENT),
                    a.getColor(R.styleable.StatefulLabelView_labelFocusedBorderColor, UNSET), normalGradient),
                selected = LabelBackgroundState(
                    a.getColor(R.styleable.StatefulLabelView_labelSelectedFillColor, UNSET),
                    a.getColor(R.styleable.StatefulLabelView_labelSelectedBorderColor, UNSET),
                    a.gradient(R.styleable.StatefulLabelView_labelSelectedGradientStartColor, null, R.styleable.StatefulLabelView_labelSelectedGradientEndColor),
                ),
                disabled = LabelBackgroundState(
                    a.getColor(R.styleable.StatefulLabelView_labelDisabledFillColor, UNSET),
                    a.getColor(R.styleable.StatefulLabelView_labelDisabledBorderColor, UNSET),
                    a.gradient(R.styleable.StatefulLabelView_labelDisabledGradientStartColor, null, R.styleable.StatefulLabelView_labelDisabledGradientEndColor),
                ),
                text = LabelTextState(
                    normalTextColor,
                    a.getColor(R.styleable.StatefulLabelView_labelPressedTextColor, UNSET),
                    a.getColor(R.styleable.StatefulLabelView_labelSelectedTextColor, UNSET),
                    a.getColor(R.styleable.StatefulLabelView_labelDisabledTextColor, UNSET),
                ),
                icons = LabelIconState(
                    a.getResourceId(R.styleable.StatefulLabelView_labelSelectedDrawableStart, 0),
                    a.getResourceId(R.styleable.StatefulLabelView_labelSelectedDrawableTop, 0),
                    a.getResourceId(R.styleable.StatefulLabelView_labelSelectedDrawableEnd, 0),
                    a.getResourceId(R.styleable.StatefulLabelView_labelSelectedDrawableBottom, 0),
                    a.getDimensionPixelSize(R.styleable.StatefulLabelView_labelDrawableWidth, 0),
                    a.getDimensionPixelSize(R.styleable.StatefulLabelView_labelDrawableHeight, 0),
                ),
            )
            }
        } finally {
            // OEM TypedArray implementations, such as MIUI's, may not implement AutoCloseable.
            attributes.recycle()
        }
    }

    private fun buildBackground(config: LabelAppearance): Drawable {
        val pressed = config.pressed.resolve(config.normal, config.derivePressed)
        val selectedPressed = config.pressed.resolve(config.selected, config.derivePressed)
        return StateListDrawable().apply {
            if (config.pressed.fillColor != UNSET || pressed.gradient.enabled) {
                addState(intArrayOf(AndroidR.attr.state_pressed, AndroidR.attr.state_selected), createBackground(selectedPressed, config))
                addState(intArrayOf(AndroidR.attr.state_pressed), createBackground(pressed, config))
            }
            if (config.focused.borderColor != UNSET) addState(intArrayOf(AndroidR.attr.state_focused), createBackground(config.focused, config))
            if (config.disabled.fillColor != UNSET || config.disabled.gradient.enabled) addState(intArrayOf(-AndroidR.attr.state_enabled), createBackground(config.disabled, config))
            if (config.selected.isConfigured) addState(intArrayOf(AndroidR.attr.state_selected), createBackground(config.selected, config))
            addState(intArrayOf(), createBackground(config.normal, config))
        }
    }

    private fun createBackground(state: LabelBackgroundState, config: LabelAppearance): GradientDrawable =
        if (state.gradient.enabled) GradientDrawable(config.orientation(), state.gradient.colors()).apply {
            shape = GradientDrawable.RECTANGLE; cornerRadii = config.corners.radii(); gradientType = GradientDrawable.LINEAR_GRADIENT
            if (config.borderWidth > 0) setStroke(config.borderWidth, state.borderColor)
        } else GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE; cornerRadii = config.corners.radii(); setColor(state.fillColor)
            if (config.borderWidth > 0) setStroke(config.borderWidth, state.borderColor)
        }

    private fun applyTextColors(state: LabelTextState) {
        if (!state.hasOverrides) return
        val states = mutableListOf<IntArray>(); val colors = mutableListOf<Int>()
        if (state.pressed != UNSET) { states += intArrayOf(AndroidR.attr.state_pressed); colors += state.pressed }
        if (state.selected != UNSET) { states += intArrayOf(AndroidR.attr.state_selected); colors += state.selected }
        if (state.disabled != UNSET) { states += intArrayOf(-AndroidR.attr.state_enabled); colors += state.disabled }
        states += intArrayOf(); colors += state.normal
        setTextColor(ColorStateList(states.toTypedArray(), colors.toIntArray()))
    }

    private fun applyStatefulDrawables(icons: LabelIconState) {
        val drawables = arrayOf(icons.start, icons.top, icons.end, icons.bottom).mapIndexed { index, selected ->
            val normal = normalDrawables[index]
            if (selected == 0) normal else StateListDrawable().apply {
                addState(intArrayOf(AndroidR.attr.state_selected), AppCompatResources.getDrawable(context, selected))
                addState(intArrayOf(), normal)
            }
        }
        // StateListDrawable starts without bounds when passed to the raw setter.
        // The intrinsic-bounds overload preserves normal and selected compound icons.
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawables[0],
            drawables[1],
            drawables[2],
            drawables[3],
        )
        updateCompoundDrawableBounds()
    }

    private fun updateCompoundDrawableBounds() {
        val icons = appearance.icons
        if (icons.width <= 0 && icons.height <= 0) return
        compoundDrawablesRelative.forEach { drawable -> drawable?.setSizedBounds(icons.width, icons.height) }
        invalidate()
    }

    private fun android.content.res.TypedArray.dimensionOr(index: Int, fallback: Int) =
        if (hasValue(index)) getDimensionPixelOffset(index, fallback) else fallback
    private fun android.content.res.TypedArray.gradient(start: Int, center: Int?, end: Int) = LabelGradient(getColor(start, UNSET), center?.takeIf(::hasValue)?.let { getColor(it, UNSET) } ?: UNSET, getColor(end, UNSET))
    private fun LabelBackgroundState.resolve(source: LabelBackgroundState, derive: Boolean): LabelBackgroundState {
        if (!derive) return this
        if (!gradient.enabled && source.gradient.enabled) {
            return copy(gradient = LabelGradient(source.gradient.start.withAlpha(), UNSET, source.gradient.end.withAlpha()))
        }
        return if (fillColor == UNSET) copy(fillColor = source.fillColor.withAlpha()) else this
    }
    private fun Int.withAlpha() = Color.argb(127, Color.red(this), Color.green(this), Color.blue(this))
    private fun LabelAppearance.orientation() = when (direction) {
        0 -> GradientDrawable.Orientation.LEFT_RIGHT
        1 -> GradientDrawable.Orientation.RIGHT_LEFT
        2 -> GradientDrawable.Orientation.TOP_BOTTOM
        3 -> GradientDrawable.Orientation.BOTTOM_TOP
        4 -> GradientDrawable.Orientation.TL_BR
        5 -> GradientDrawable.Orientation.TR_BL
        6 -> GradientDrawable.Orientation.BL_TR
        7 -> GradientDrawable.Orientation.BR_TL
        else -> GradientDrawable.Orientation.LEFT_RIGHT
    }
    private fun LabelCorners.radii() = floatArrayOf(topLeft.toFloat(), topLeft.toFloat(), topRight.toFloat(), topRight.toFloat(), bottomRight.toFloat(), bottomRight.toFloat(), bottomLeft.toFloat(), bottomLeft.toFloat())
    private fun LabelGradient.colors() = if (center != UNSET) intArrayOf(start, center, end) else intArrayOf(start, end)
    private fun Drawable.setSizedBounds(width: Int, height: Int) { setBounds(0, 0, width.takeIf { it > 0 } ?: intrinsicWidth, height.takeIf { it > 0 } ?: intrinsicHeight) }

    private data class LabelAppearance(val corners: LabelCorners, val borderWidth: Int, val direction: Int, val derivePressed: Boolean, val normal: LabelBackgroundState, val pressed: LabelBackgroundState, val focused: LabelBackgroundState, val selected: LabelBackgroundState, val disabled: LabelBackgroundState, val text: LabelTextState, val icons: LabelIconState)
    private data class LabelCorners(val topLeft: Int, val topRight: Int, val bottomRight: Int, val bottomLeft: Int)
    private data class LabelBackgroundState(val fillColor: Int, val borderColor: Int, val gradient: LabelGradient) { val isConfigured get() = fillColor != UNSET || borderColor != UNSET || gradient.enabled }
    private data class LabelGradient(val start: Int, val center: Int, val end: Int) { val enabled get() = start != UNSET }
    private data class LabelTextState(val normal: Int, val pressed: Int, val selected: Int, val disabled: Int) { val hasOverrides get() = pressed != UNSET || selected != UNSET || disabled != UNSET }
    private data class LabelIconState(val start: Int, val top: Int, val end: Int, val bottom: Int, val width: Int, val height: Int)
    private companion object { const val UNSET = 0 }
}
