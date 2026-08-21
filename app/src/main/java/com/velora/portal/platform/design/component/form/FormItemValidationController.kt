package com.velora.portal.platform.design.component.form

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.velora.portal.platform.design.component.StyledEditTextView

internal class FormItemValidationController(
    private val input: StyledEditTextView,
    private val errorView: TextView,
    private val onErrorStateChanged: (Boolean) -> Unit,
) {

    fun bindTextChange() {
        input.doAfterTextChanged {
            hideError()
        }
    }

    fun showError() {
        errorView.isVisible = true
        input.isSelected = true
        onErrorStateChanged(true)
    }

    fun hideError() {
        errorView.isVisible = false
        input.isSelected = false
        onErrorStateChanged(false)
    }
}
