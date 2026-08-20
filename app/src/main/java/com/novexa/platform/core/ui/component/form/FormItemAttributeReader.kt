package com.novexa.platform.core.ui.component.form

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.novexa.platform.R

internal class FormItemAttributeReader(
    private val context: Context,
) {

    fun read(attrs: AttributeSet?): FormItemAttributes {
        lateinit var result: FormItemAttributes
        context.withStyledAttributes(attrs, R.styleable.FormItemView) {
            result = FormItemAttributes(
                title = getText(R.styleable.FormItemView_titleText),
                hint = getText(R.styleable.FormItemView_hintText),
                errorText = getText(R.styleable.FormItemView_errorText),
                prefixText = getText(R.styleable.FormItemView_prefixText),
                mode = getInt(R.styleable.FormItemView_formType, 0).toFormMode(),
                inputType = getInt(R.styleable.FormItemView_inputType, 0).toFormInputType(),
                maxLength = getInt(R.styleable.FormItemView_editMaxLength, -1),
                showContactIcon = getBoolean(R.styleable.FormItemView_showContactIcon, false),
                endIconRes = getResourceId(R.styleable.FormItemView_formEndIcon, 0)
                    .takeIf { it != 0 },
                inputBackgroundColor = if (hasValue(R.styleable.FormItemView_inputBackgroundColor)) {
                    getColor(R.styleable.FormItemView_inputBackgroundColor, Color.TRANSPARENT)
                } else {
                    null
                },
                inputStrokeColor = if (hasValue(R.styleable.FormItemView_inputStrokeColor)) {
                    getColor(R.styleable.FormItemView_inputStrokeColor, Color.TRANSPARENT)
                } else {
                    null
                },
                inputFocusedStrokeColor = if (hasValue(R.styleable.FormItemView_inputFocusedStrokeColor)) {
                    getColor(R.styleable.FormItemView_inputFocusedStrokeColor, Color.TRANSPARENT)
                } else {
                    null
                },
            )
        }
        return result
    }

    private fun Int.toFormMode(): FormMode {
        return if (this == 1) FormMode.SELECT else FormMode.INPUT
    }

    private fun Int.toFormInputType(): FormInputType {
        return when (this) {
            1 -> FormInputType.NUMBER
            2 -> FormInputType.PHONE
            else -> if (this == 3) FormInputType.EMAIL else FormInputType.TEXT
        }
    }
}
