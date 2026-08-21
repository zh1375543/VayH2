package com.velora.portal.core.ui.extension

import androidx.core.widget.NestedScrollView

fun NestedScrollView.isScrollViewAtBottom(): Boolean {
    val view = this.getChildAt(0)
    return view != null && (this.scrollY + this.height) >= view.height
}
