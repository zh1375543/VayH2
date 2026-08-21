package com.velora.portal.platform.design.component

import android.R as AndroidR
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.velora.portal.R

/** A ConstraintLayout with a self-contained, state-aware panel background. */
class SurfacePanelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val drawableSet = buildDrawableSet(createAppearance(context, attrs))

    init {
        background = drawableSet.stateList
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        drawableSet.updateBounds(width, height)
    }

    private fun createAppearance(context: Context, attrs: AttributeSet?): PanelAppearance {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SurfacePanelLayout)
        return try {
            val radius = attributes.getDimensionPixelOffset(
                R.styleable.SurfacePanelLayout_panelCornerRadius,
                DEFAULT_DIMENSION,
            )
            val normalGradient = attributes.readGradient(
                R.styleable.SurfacePanelLayout_panelGradientStartColor,
                R.styleable.SurfacePanelLayout_panelGradientEndColor,
                R.styleable.SurfacePanelLayout_panelGradientCenterColor,
            )
            val normalFill = attributes.getColor(
                R.styleable.SurfacePanelLayout_panelFillColor,
                Color.TRANSPARENT,
            )
            PanelAppearance(
                corners = PanelCorners(
                    topLeft = attributes.cornerOrRadius(
                        R.styleable.SurfacePanelLayout_panelTopLeftRadius,
                        radius,
                    ),
                    topRight = attributes.cornerOrRadius(
                        R.styleable.SurfacePanelLayout_panelTopRightRadius,
                        radius,
                    ),
                    bottomRight = attributes.cornerOrRadius(
                        R.styleable.SurfacePanelLayout_panelBottomRightRadius,
                        radius,
                    ),
                    bottomLeft = attributes.cornerOrRadius(
                        R.styleable.SurfacePanelLayout_panelBottomLeftRadius,
                        radius,
                    ),
                ),
                borderWidth = attributes.getDimensionPixelOffset(
                    R.styleable.SurfacePanelLayout_panelBorderWidth,
                    DEFAULT_DIMENSION,
                ),
                gradientDirection = PanelGradientDirection.fromAttributeValue(
                    attributes.getInt(
                        R.styleable.SurfacePanelLayout_panelGradientDirection,
                        PanelGradientDirection.LEFT_TO_RIGHT.attributeValue,
                    ),
                ),
                derivePressedState = attributes.getBoolean(
                    R.styleable.SurfacePanelLayout_panelDerivePressedState,
                    false,
                ),
                normal = PanelStateStyle(
                    fillColor = normalFill,
                    borderColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelBorderColor,
                        Color.TRANSPARENT,
                    ),
                    gradient = normalGradient,
                ),
                pressed = PanelStateStyle(
                    fillColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelPressedFillColor,
                        UNSET_COLOR,
                    ),
                    borderColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelPressedBorderColor,
                        UNSET_COLOR,
                    ),
                    gradient = attributes.readGradient(
                        R.styleable.SurfacePanelLayout_panelPressedGradientStartColor,
                        R.styleable.SurfacePanelLayout_panelPressedGradientEndColor,
                    ),
                ),
                focused = PanelStateStyle(
                    fillColor = normalFill,
                    borderColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelFocusedBorderColor,
                        UNSET_COLOR,
                    ),
                    gradient = normalGradient,
                ),
                selected = PanelStateStyle(
                    fillColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelSelectedFillColor,
                        UNSET_COLOR,
                    ),
                    borderColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelSelectedBorderColor,
                        UNSET_COLOR,
                    ),
                    gradient = attributes.readGradient(
                        R.styleable.SurfacePanelLayout_panelSelectedGradientStartColor,
                        R.styleable.SurfacePanelLayout_panelSelectedGradientEndColor,
                    ),
                ),
                disabled = PanelStateStyle(
                    fillColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelDisabledFillColor,
                        UNSET_COLOR,
                    ),
                    borderColor = attributes.getColor(
                        R.styleable.SurfacePanelLayout_panelDisabledBorderColor,
                        UNSET_COLOR,
                    ),
                    gradient = attributes.readGradient(
                        R.styleable.SurfacePanelLayout_panelDisabledGradientStartColor,
                        R.styleable.SurfacePanelLayout_panelDisabledGradientEndColor,
                    ),
                ),
            )
        } finally {
            attributes.recycle()
        }
    }

    private fun TypedArray.cornerOrRadius(index: Int, radius: Int): Int {
        return getDimensionPixelOffset(index, DEFAULT_DIMENSION).let { value ->
            if (value == DEFAULT_DIMENSION) radius else value
        }
    }

    private fun TypedArray.readGradient(
        startIndex: Int,
        endIndex: Int,
        centerIndex: Int? = null,
    ): PanelGradient {
        return PanelGradient(
            startColor = getColor(startIndex, UNSET_COLOR),
            centerColor = centerIndex?.let { index ->
                if (hasValue(index)) getColor(index, UNSET_COLOR) else UNSET_COLOR
            } ?: UNSET_COLOR,
            endColor = getColor(endIndex, UNSET_COLOR),
        )
    }

    private fun buildDrawableSet(appearance: PanelAppearance): PanelDrawableSet {
        val resolvedPressed = appearance.resolvePressedState(isSelected)
        val normal = appearance.normal.toDrawable(appearance)
        val pressed = resolvedPressed.toDrawable(appearance)
        val focused = appearance.focused.toDrawable(appearance)
        val disabled = appearance.disabled.toDrawable(appearance)
        val selected = appearance.selected.toDrawable(appearance)
        return PanelDrawableSet(
            normal = normal,
            pressed = pressed,
            focused = focused,
            disabled = disabled,
            selected = selected,
            stateList = StateListDrawable().apply {
                if (appearance.pressed.fillColor != UNSET_COLOR || resolvedPressed.gradient.isEnabled) {
                    addState(intArrayOf(AndroidR.attr.state_pressed), pressed)
                }
                if (appearance.focused.borderColor != UNSET_COLOR) {
                    addState(intArrayOf(AndroidR.attr.state_focused), focused)
                }
                if (appearance.disabled.fillColor != UNSET_COLOR || appearance.disabled.gradient.isEnabled) {
                    addState(intArrayOf(-AndroidR.attr.state_enabled), disabled)
                }
                if (
                    appearance.selected.fillColor != UNSET_COLOR ||
                    appearance.selected.borderColor != UNSET_COLOR ||
                    appearance.selected.gradient.isEnabled
                ) {
                    addState(intArrayOf(AndroidR.attr.state_selected), selected)
                }
                addState(intArrayOf(), normal)
            },
        )
    }

    private fun PanelAppearance.resolvePressedState(isSelectedAtCreation: Boolean): PanelStateStyle {
        if (!derivePressedState) return pressed
        return when {
            pressed.gradient.startColor == UNSET_COLOR && normal.gradient.startColor != UNSET_COLOR -> {
                pressed.copy(
                    gradient = PanelGradient(
                        startColor = normal.gradient.startColor.withAlphaFraction(PRESSED_ALPHA),
                        endColor = normal.gradient.endColor.withAlphaFraction(PRESSED_ALPHA),
                    ),
                )
            }

            pressed.fillColor == UNSET_COLOR -> {
                val sourceColor = if (isSelectedAtCreation && selected.fillColor != UNSET_COLOR) {
                    selected.fillColor
                } else {
                    normal.fillColor
                }
                pressed.copy(fillColor = sourceColor.withAlphaFraction(PRESSED_ALPHA))
            }

            else -> pressed
        }
    }

    private fun PanelStateStyle.toDrawable(appearance: PanelAppearance): GradientDrawable {
        return if (gradient.isEnabled) {
            GradientDrawable(appearance.gradientDirection.toPlatformOrientation(), gradient.toColorArray())
        } else {
            GradientDrawable().apply { setColor(fillColor) }
        }.apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = appearance.corners.toPlatformRadii()
            gradientType = GradientDrawable.LINEAR_GRADIENT
            if (appearance.borderWidth > 0) setStroke(appearance.borderWidth, borderColor)
        }
    }

    private fun PanelGradient.toColorArray(): IntArray = if (centerColor != UNSET_COLOR) {
        intArrayOf(startColor, centerColor, endColor)
    } else {
        intArrayOf(startColor, endColor)
    }

    private fun PanelCorners.toPlatformRadii(): FloatArray = floatArrayOf(
        topLeft.toFloat(), topLeft.toFloat(), topRight.toFloat(), topRight.toFloat(),
        bottomRight.toFloat(), bottomRight.toFloat(), bottomLeft.toFloat(), bottomLeft.toFloat(),
    )

    private fun PanelGradientDirection.toPlatformOrientation(): GradientDrawable.Orientation = when (this) {
        PanelGradientDirection.LEFT_TO_RIGHT -> GradientDrawable.Orientation.LEFT_RIGHT
        PanelGradientDirection.RIGHT_TO_LEFT -> GradientDrawable.Orientation.RIGHT_LEFT
        PanelGradientDirection.TOP_TO_BOTTOM -> GradientDrawable.Orientation.TOP_BOTTOM
        PanelGradientDirection.BOTTOM_TO_TOP -> GradientDrawable.Orientation.BOTTOM_TOP
        PanelGradientDirection.TOP_LEFT_TO_BOTTOM_RIGHT -> GradientDrawable.Orientation.TL_BR
        PanelGradientDirection.TOP_RIGHT_TO_BOTTOM_LEFT -> GradientDrawable.Orientation.TR_BL
        PanelGradientDirection.BOTTOM_LEFT_TO_TOP_RIGHT -> GradientDrawable.Orientation.BL_TR
        PanelGradientDirection.BOTTOM_RIGHT_TO_TOP_LEFT -> GradientDrawable.Orientation.BR_TL
    }

    private fun Int.withAlphaFraction(opacity: Float): Int = Color.argb(
        (255 * opacity).toInt(), Color.red(this), Color.green(this), Color.blue(this),
    )

    private data class PanelAppearance(
        val corners: PanelCorners,
        val borderWidth: Int,
        val gradientDirection: PanelGradientDirection,
        val derivePressedState: Boolean,
        val normal: PanelStateStyle,
        val pressed: PanelStateStyle,
        val focused: PanelStateStyle,
        val selected: PanelStateStyle,
        val disabled: PanelStateStyle,
    )

    private data class PanelCorners(
        val topLeft: Int,
        val topRight: Int,
        val bottomRight: Int,
        val bottomLeft: Int,
    )

    private data class PanelStateStyle(
        val fillColor: Int,
        val borderColor: Int,
        val gradient: PanelGradient,
    )

    private data class PanelGradient(
        val startColor: Int = UNSET_COLOR,
        val centerColor: Int = UNSET_COLOR,
        val endColor: Int = UNSET_COLOR,
    ) {
        val isEnabled: Boolean get() = startColor != UNSET_COLOR && endColor != UNSET_COLOR
    }

    private data class PanelDrawableSet(
        val normal: GradientDrawable,
        val pressed: GradientDrawable,
        val focused: GradientDrawable,
        val disabled: GradientDrawable,
        val selected: GradientDrawable,
        val stateList: StateListDrawable,
    ) {
        fun updateBounds(width: Int, height: Int) {
            listOf(normal, pressed, focused, disabled, selected).forEach {
                it.setBounds(0, 0, width, height)
            }
            stateList.setBounds(0, 0, width, height)
        }
    }

    private enum class PanelGradientDirection(val attributeValue: Int) {
        LEFT_TO_RIGHT(0), RIGHT_TO_LEFT(1), TOP_TO_BOTTOM(2), BOTTOM_TO_TOP(3),
        TOP_LEFT_TO_BOTTOM_RIGHT(4), TOP_RIGHT_TO_BOTTOM_LEFT(5),
        BOTTOM_LEFT_TO_TOP_RIGHT(6), BOTTOM_RIGHT_TO_TOP_LEFT(7),
        ;

        companion object {
            fun fromAttributeValue(value: Int): PanelGradientDirection {
                return entries.firstOrNull { it.attributeValue == value } ?: LEFT_TO_RIGHT
            }
        }
    }

    private companion object {
        const val DEFAULT_DIMENSION = 0
        const val UNSET_COLOR = Color.TRANSPARENT
        const val PRESSED_ALPHA = 0.5f
    }
}
