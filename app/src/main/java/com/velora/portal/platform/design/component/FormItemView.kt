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
        binding.etInput.hint = attributes.hint
        binding.tvError.text = attributes.errorText
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

        if (!hasPrefix) {
            binding.etInput.updatePaddingRelative(start = defaultInputPaddingStart)
            return
        }

        binding.prefixContainer.doOnLayout {
            binding.etInput.updatePaddingRelative(
                start = defaultInputPaddingStart + it.width + PREFIX_INPUT_SPACING_DP.dp,
            )
        }
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
        ivErrorIcon.isVisible = hasError
        ivEndIcon.isVisible = hasExternalEndIcon && !hasError
        etInput.updatePaddingRelative(
            end = if (hasError) {
                defaultInputPaddingEnd + ERROR_ICON_RESERVED_WIDTH_DP.dp
            } else {
                defaultInputPaddingEnd
            },
        )
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private companion object {
        const val PREFIX_INPUT_SPACING_DP = 12
        const val ERROR_ICON_RESERVED_WIDTH_DP = 44
    }
}
