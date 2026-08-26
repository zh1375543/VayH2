package com.velora.portal.platform.design.component.form

internal data class FormItemAttributes(
    val title: CharSequence?,
    val hint: CharSequence?,
    val errorText: CharSequence?,
    val prefixText: CharSequence?,
    val mode: FormMode,
    val inputType: FormInputType,
    val maxLength: Int,
    val showErrorIcon: Boolean,
    val showContactIcon: Boolean,
    val startIconRes: Int?,
    val endIconRes: Int?,
    val inputBackgroundColor: Int?,
    val inputStrokeColor: Int?,
    val inputFocusedStrokeColor: Int?,
    val titleTextColor: Int?,
)

internal enum class FormMode {
    INPUT,
    SELECT,
}

internal enum class FormInputType {
    TEXT,
    NUMBER,
    PHONE,
    EMAIL,
}
