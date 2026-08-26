package com.velora.portal.platform.design.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.doOnLayout
import androidx.core.view.updatePaddingRelative
import com.velora.portal.databinding.ViewFormEntryBinding
import com.velora.portal.platform.design.component.form.FormItemAttributeReader
import com.velora.portal.platform.design.component.form.FormItemAttributes
import com.velora.portal.platform.design.component.form.FormItemModeController
import com.velora.portal.platform.design.component.form.FormItemValidationController
import com.velora.portal.platform.design.extension.singleClick

class FormItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: ViewFormEntryBinding
    private val attributes: FormItemAttributes
    private val modeController: FormItemModeController
    private val validationController: FormItemValidationController
    private val defaultInputPaddingStart: Int
    private val defaultInputPaddingEnd: Int
    private var hasExternalEndIcon = false
    private var hasStartIcon = false

    init {
        orientation = VERTICAL
        binding = ViewFormEntryBinding.inflate(LayoutInflater.from(context), this)
        defaultInputPaddingStart = binding.etInput.paddingStart
        defaultInputPaddingEnd = binding.etInput.paddingEnd
        attributes = FormItemAttributeReader(context).read(attrs)
        modeController = FormItemModeController(binding.etInput)
        validationController = FormItemValidationController(
            input = binding.etInput,
            errorView = binding.tvError,
            onErrorStateChanged = { hasError ->
                modeController.onValidationStateChanged(hasError)
                updateValidationIcon(hasError)
            },
        )

        binding.tvTitle.text = attributes.title
        attributes.titleTextColor?.let { binding.tvTitle.setTextColor(it) }
        binding.etInput.hint = attributes.hint
        binding.tvError.text = attributes.errorText
        hasStartIcon = attributes.startIconRes != null
        binding.ivStartIcon.isVisible = hasStartIcon
        attributes.startIconRes?.let(binding.ivStartIcon::setImageResource)
        setPrefixText(attributes.prefixText)
        hasExternalEndIcon = attributes.showContactIcon || attributes.endIconRes != null
        binding.ivEndIcon.isVisible = hasExternalEndIcon
        attributes.endIconRes?.let(binding.ivEndIcon::setImageResource)
        attributes.inputBackgroundColor?.let(binding.etInput::setSolidColor)
        attributes.inputStrokeColor?.let(binding.etInput::setStrokeColor)
        attributes.inputFocusedStrokeColor?.let(binding.etInput::setFocusedStrokeColor)

        modeController.apply(attributes)
        validationController.bindTextChange()
    }

    fun getText(): String {
        return binding.etInput.text?.toString() ?: ""
    }

    fun setText(text: String?) {
        binding.etInput.setText(text)
        modeController.restoreSelectIndicatorIfNeeded()
    }

    fun setTitle(text: CharSequence?) {
        binding.tvTitle.text = text ?: ""
    }

    /** Displays a non-editable prefix such as a country calling code before the input value. */
    fun setPrefixText(text: CharSequence?) {
        val hasPrefix = !text.isNullOrBlank()
        binding.prefixContainer.isVisible = hasPrefix
        binding.tvPrefix.text = text ?: ""
        binding.prefixContainer.translationX = if (hasStartIcon) {
            START_ICON_RESERVED_WIDTH_DP.dp.toFloat()
        } else {
            0f
        }
        binding.prefixContainer.doOnLayout { updateInputStartPadding() }
        updateInputStartPadding()
    }

    fun showError() {
        validationController.showError()
    }

    fun showError(message: CharSequence) {
        binding.tvError.text = message
        validationController.showError()
    }

    fun setEnableEdit(enable: Boolean) {
        modeController.setEnabled(enable)
        if (!enable) {
            validationController.hideError()
        }
    }

    fun hideError() {
        validationController.hideError()
    }

    fun setContactClick(block: () -> Unit) {
        binding.ivEndIcon.singleClick { block() }
    }

    fun setContactVisible(isVisible: Boolean) {
        hasExternalEndIcon = isVisible
        binding.ivEndIcon.isVisible = isVisible && !binding.ivErrorIcon.isVisible
        modeController.setUsesExternalEndIcon(isVisible)
    }

    fun setEndIcon(@DrawableRes imageRes: Int?) {
        hasExternalEndIcon = imageRes != null
        binding.ivEndIcon.isVisible = imageRes != null && !binding.ivErrorIcon.isVisible
        imageRes?.let(binding.ivEndIcon::setImageResource)
        modeController.setUsesExternalEndIcon(imageRes != null)
    }

    /** Shows an optional icon before the input text without changing existing form fields. */
    fun setStartIcon(@DrawableRes imageRes: Int?) {
        hasStartIcon = imageRes != null
        binding.ivStartIcon.isVisible = hasStartIcon
        imageRes?.let(binding.ivStartIcon::setImageResource)
        binding.prefixContainer.translationX = if (hasStartIcon) {
            START_ICON_RESERVED_WIDTH_DP.dp.toFloat()
        } else {
            0f
        }
        updateInputStartPadding()
    }

    fun setEndIconClick(block: () -> Unit) {
        binding.ivEndIcon.singleClick { block() }
    }

    fun setOnClick(block: () -> Unit) {
        binding.etInput.singleClick { block() }
    }

    fun getEditText(): StyledEditTextView = binding.etInput

    fun setInputBackgroundColor(@ColorInt color: Int) {
        binding.etInput.setSolidColor(color)
    }

    fun setInputStrokeColor(@ColorInt color: Int) {
        binding.etInput.setStrokeColor(color)
    }

    fun setInputFocusedStrokeColor(@ColorInt color: Int) {
        binding.etInput.setFocusedStrokeColor(color)
    }

    fun setDrawableTint(@ColorRes color: Int) {
        modeController.setDrawableTint(color)
    }

    private fun updateValidationIcon(hasError: Boolean) = with(binding) {
        val showErrorIcon = hasError && attributes.showErrorIcon
        ivErrorIcon.isVisible = showErrorIcon
        ivEndIcon.isVisible = hasExternalEndIcon && !showErrorIcon
        etInput.updatePaddingRelative(
            end = if (showErrorIcon) {
                defaultInputPaddingEnd + ERROR_ICON_RESERVED_WIDTH_DP.dp
            } else {
                defaultInputPaddingEnd
            },
        )
    }

    private fun updateInputStartPadding() {
        val hasPrefix = binding.prefixContainer.isVisible
        val prefixWidth = if (hasPrefix) {
            binding.prefixContainer.width + PREFIX_INPUT_SPACING_DP.dp
        } else {
            0
        }
        val inputStart = when {
            hasStartIcon && hasPrefix -> {
                START_ICON_RESERVED_WIDTH_DP.dp + defaultInputPaddingStart
            }
            hasStartIcon -> START_ICON_RESERVED_WIDTH_DP.dp
            else -> defaultInputPaddingStart
        }
        binding.etInput.updatePaddingRelative(
            start = inputStart + prefixWidth,
        )
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private companion object {
        const val PREFIX_INPUT_SPACING_DP = 12
        const val START_ICON_RESERVED_WIDTH_DP = 44
        const val ERROR_ICON_RESERVED_WIDTH_DP = 44
    }
}
