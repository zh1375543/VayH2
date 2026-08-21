package com.velora.portal.core.common.util.context

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Context.resolveColorCompat(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)
